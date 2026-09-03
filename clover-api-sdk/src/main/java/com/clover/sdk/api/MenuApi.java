package com.clover.sdk.api;

import com.clover.sdk.exception.CloverException;
import com.clover.sdk.internal.CloverHttpClient;
import com.clover.sdk.internal.CloverList;
import com.clover.sdk.model.response.Category;
import com.clover.sdk.model.response.Menu;
import com.clover.sdk.model.response.MenuItem;
import com.clover.sdk.model.response.ModifierGroup;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MenuApi {

    private static final int PAGE_SIZE = 500;

    private final CloverHttpClient http;

    public MenuApi(CloverHttpClient http) {
        this.http = http;
    }

    /**
     * Returns all menus configured for this merchant.
     * Use the returned menu IDs to fetch items for a specific menu.
     */
    public List<Menu> getMenus() throws CloverException {
        String appId = http.getAppId();
        String path = "/v3/merchants/" + http.getMerchantId() + "/menus"
                + (appId != null && !appId.isBlank() ? "?appId=" + appId : "");
        JsonNode node = http.get(path);
        return parseElements(node, new TypeReference<CloverList<Menu>>() {});
    }

    /**
     * Returns all modifier groups with their individual modifiers expanded.
     */
    public List<ModifierGroup> getModifierGroupsWithModifiers() throws CloverException {
        String path = "/v3/merchants/" + http.getMerchantId() + "/modifier_groups?expand=modifiers";
        JsonNode node = http.get(path);
        return parseElements(node, new TypeReference<CloverList<ModifierGroup>>() {});
    }

    /**
     * Returns all menu categories, each containing references to the items in that category.
     */
    public List<Category> getCategories() throws CloverException {
        String path = "/v3/merchants/" + http.getMerchantId() + "/categories?expand=items";
        JsonNode node = http.get(path);
        return parseElements(node, new TypeReference<CloverList<Category>>() {});
    }

    /**
     * Returns all menu items (fetches all pages automatically).
     * Items are expanded with their category and modifier group data.
     */
    public List<MenuItem> getMenuItems() throws CloverException {
        List<MenuItem> all = new ArrayList<>();
        int offset = 0;
        while (true) {
            String path = "/v3/merchants/" + http.getMerchantId()
                    + "/items?expand=categories,modifierGroups&limit=" + PAGE_SIZE + "&offset=" + offset;
            JsonNode node = http.get(path);
            List<MenuItem> batch = parseElements(node, new TypeReference<CloverList<MenuItem>>() {});
            all.addAll(batch);
            if (batch.size() < PAGE_SIZE) break;
            offset += PAGE_SIZE;
        }
        return all;
    }

    /**
     * Returns only items that belong to at least one category and are available for ordering.
     * Items with no category are inventory-only and excluded from the menu.
     */
    public List<MenuItem> getAvailableMenuItems() throws CloverException {
        return getMenuItems().stream()
                .filter(MenuItem::isAvailableForOrdering)
                .filter(i -> !i.getCategoryList().isEmpty())
                .collect(Collectors.toList());
    }

    private <T> List<T> parseElements(JsonNode node, TypeReference<CloverList<T>> typeRef) throws CloverException {
        try {
            CloverList<T> page = http.mapper.readValue(http.mapper.treeAsTokens(node), typeRef);
            return page.getElements();
        } catch (Exception e) {
            throw new CloverException("Failed to parse Clover menu response", e);
        }
    }
}
