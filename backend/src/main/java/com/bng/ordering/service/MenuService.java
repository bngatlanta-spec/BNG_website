package com.bng.ordering.service;

import com.bng.ordering.model.MenuCategory;
import com.bng.ordering.model.MenuModifier;
import com.bng.ordering.model.MenuModifierGroup;
import com.bng.ordering.model.MenuItem;
import com.bng.ordering.model.response.MenuResponse;
import com.clover.sdk.CloverClient;
import com.clover.sdk.exception.CloverException;
import com.clover.sdk.model.response.Menu;
import com.clover.sdk.model.response.ModifierGroup;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final CloverClient cloverClient;

    public MenuService(CloverClient cloverClient) {
        this.cloverClient = cloverClient;
    }

    public List<Menu> getMenus() {
        try {
            return cloverClient.getMenus();
        } catch (CloverException e) {
            throw new RuntimeException("Failed to fetch menus from Clover: " + e.getMessage(), e);
        }
    }

    public MenuResponse getFullMenu() {
        try {
            List<com.clover.sdk.model.response.MenuItem> sdkItems = cloverClient.getAvailableMenuItems();

            // Build a map of modifier group id -> full group with modifiers
            Map<String, MenuModifierGroup> modGroupMap = buildModifierGroupMap();

            List<MenuCategory> categories = groupByCategory(sdkItems, modGroupMap);
            return MenuResponse.builder()
                .categories(categories)
                .totalItems(sdkItems.size())
                .build();
        } catch (CloverException e) {
            throw new RuntimeException("Failed to fetch menu from Clover: " + e.getMessage(), e);
        }
    }

    private Map<String, MenuModifierGroup> buildModifierGroupMap() {
        try {
            List<ModifierGroup> sdkGroups = cloverClient.getModifierGroupsWithModifiers();
            Map<String, MenuModifierGroup> map = new LinkedHashMap<>();
            for (ModifierGroup g : sdkGroups) {
                List<MenuModifier> modifiers = g.getModifierList().stream()
                    .map(m -> MenuModifier.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .price(m.getPrice() != null ? m.getPrice() / 100.0 : 0.0)
                        .build())
                    .collect(Collectors.toList());
                map.put(g.getId(), MenuModifierGroup.builder()
                    .id(g.getId())
                    .name(g.getName())
                    .minRequired(g.getMinRequired() != null ? g.getMinRequired() : 0)
                    .maxAllowed(g.getMaxAllowed() != null ? g.getMaxAllowed() : 99)
                    .modifiers(modifiers)
                    .build());
            }
            return map;
        } catch (CloverException e) {
            // Non-fatal — menu still loads without modifier data
            return Map.of();
        }
    }

    public List<MenuCategory> getCategories() {
        return getFullMenu().getCategories().stream()
            .map(c -> MenuCategory.builder()
                .id(c.getId()).name(c.getName()).slug(c.getSlug()).build())
            .collect(Collectors.toList());
    }


    public Optional<MenuItem> getItemById(String id) {
        try {
            return getFullMenu().getCategories().stream()
                .flatMap(c -> c.getItems().stream())
                .filter(i -> id.equals(i.getId()))
                .findFirst();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private List<MenuCategory> groupByCategory(List<com.clover.sdk.model.response.MenuItem> sdkItems,
                                               Map<String, MenuModifierGroup> modGroupMap) {
        Map<String, List<com.clover.sdk.model.response.MenuItem>> byCat = new LinkedHashMap<>();
        for (com.clover.sdk.model.response.MenuItem item : sdkItems) {
            List<com.clover.sdk.model.response.Category> cats = item.getCategoryList();
            String catName = cats.isEmpty() ? "Other" : cats.get(0).getName();
            byCat.computeIfAbsent(catName, k -> new ArrayList<>()).add(item);
        }
        return byCat.entrySet().stream()
            .map(e -> {
                String slug = slugify(e.getKey());
                List<MenuItem> items = e.getValue().stream()
                    .map(sdk -> toMenuItem(sdk, modGroupMap))
                    .collect(Collectors.toList());
                return MenuCategory.builder()
                    .id("cat-" + slug)
                    .name(e.getKey())
                    .slug(slug)
                    .items(items)
                    .build();
            })
            .collect(Collectors.toList());
    }

    private MenuItem toMenuItem(com.clover.sdk.model.response.MenuItem sdk,
                                Map<String, MenuModifierGroup> modGroupMap) {
        String catSlug = sdk.getCategoryList().isEmpty() ? "other"
            : slugify(sdk.getCategoryList().get(0).getName());

        List<MenuModifierGroup> groups = sdk.getModifierGroupList().stream()
            .map(g -> modGroupMap.get(g.getId()))
            .filter(g -> g != null && !g.getModifiers().isEmpty())
            .collect(Collectors.toList());

        return MenuItem.builder()
            .id(sdk.getId())
            .name(sdk.getName())
            .description("")
            .price(sdk.getPrice() != null ? sdk.getPrice() / 100.0 : 0.0)
            .category(catSlug)
            .imageUrl(sdk.getImageUrl())
            .vegetarian(false)
            .spicy(false)
            .popular(false)
            .modifierGroups(groups)
            .build();
    }

    private String slugify(String name) {
        if (name == null) return "other";
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
