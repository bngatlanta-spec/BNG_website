package com.bng.ordering.controller;

import com.bng.ordering.model.MenuCategory;
import com.bng.ordering.model.MenuItem;
import com.bng.ordering.model.response.ApiResponse;
import com.bng.ordering.model.response.MenuResponse;
import com.bng.ordering.service.MenuService;
import com.clover.sdk.model.response.Menu;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /** GET /api/menu/menus — list all Clover menus for this merchant */
    @GetMapping("/menus")
    public ApiResponse<List<Menu>> getMenus() {
        return ApiResponse.ok(menuService.getMenus());
    }

    /** GET /api/menu — full menu with all categories and items */
    @GetMapping
    public ApiResponse<MenuResponse> getFullMenu() {
        return ApiResponse.ok(menuService.getFullMenu());
    }

    /** GET /api/menu/categories — category list (no items) */
    @GetMapping("/categories")
    public ApiResponse<List<MenuCategory>> getCategories() {
        return ApiResponse.ok(menuService.getCategories());
    }

    /** GET /api/menu/items/{id} — single item by id */
    @GetMapping("/items/{id}")
    public ResponseEntity<ApiResponse<MenuItem>> getItem(@PathVariable String id) {
        return menuService.getItemById(id)
            .map(item -> ResponseEntity.ok(ApiResponse.ok(item)))
            .orElse(ResponseEntity.notFound().<ApiResponse<MenuItem>>build());
    }
}
