package com.clover.sdk.model.response;

import com.clover.sdk.internal.CloverList;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;
// ModifierGroup is in the same package

@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuItem {
    private String id;
    private String name;
    private Long price;          // price in cents
    private String imageUrl;
    private Boolean hidden;
    private Boolean deleted;
    private Boolean available;
    private String priceType;

    // Clover nests these as {"elements": [...]}
    private CloverList<Category> categories;
    private CloverList<ModifierGroup> modifierGroups;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    /** Price in dollars, formatted as a string (e.g., "12.50"). */
    public String getPriceFormatted() {
        if (price == null) return "$0.00";
        return String.format("$%.2f", price / 100.0);
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getHidden() { return hidden; }
    public void setHidden(Boolean hidden) { this.hidden = hidden; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }

    public String getPriceType() { return priceType; }
    public void setPriceType(String priceType) { this.priceType = priceType; }

    public CloverList<Category> getCategories() { return categories; }
    public void setCategories(CloverList<Category> categories) { this.categories = categories; }

    public CloverList<ModifierGroup> getModifierGroups() { return modifierGroups; }
    public void setModifierGroups(CloverList<ModifierGroup> modifierGroups) { this.modifierGroups = modifierGroups; }

    /** Convenience method — unwraps the nested category list. */
    public List<Category> getCategoryList() {
        return categories != null ? categories.getElements() : Collections.emptyList();
    }

    /** Convenience method — unwraps the nested modifier group list. */
    public List<ModifierGroup> getModifierGroupList() {
        return modifierGroups != null ? modifierGroups.getElements() : Collections.emptyList();
    }

    /** Returns true if this item should be shown on a menu (not hidden, deleted, or unavailable). */
    public boolean isAvailableForOrdering() {
        return !Boolean.TRUE.equals(hidden)
                && !Boolean.TRUE.equals(deleted)
                && !Boolean.FALSE.equals(available)
                && price != null;
    }

    @Override
    public String toString() {
        return "MenuItem{id='" + id + "', name='" + name + "', price=" + getPriceFormatted() + "}";
    }
}
