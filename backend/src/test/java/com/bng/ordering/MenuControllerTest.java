package com.bng.ordering;

import com.clover.sdk.CloverClient;
import com.clover.sdk.internal.CloverList;
import com.clover.sdk.model.response.Category;
import com.clover.sdk.model.response.MenuItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MenuControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CloverClient cloverClient;

    private static final String BIRYANI_ITEM_ID = "item-biryani-001";
    private static final String STARTER_ITEM_ID = "item-starter-001";

    @BeforeEach
    void setUp() throws Exception {
        Category biryaniCat = new Category();
        biryaniCat.setId("cat-biryani");
        biryaniCat.setName("Biryani");

        Category starterCat = new Category();
        starterCat.setId("cat-starters");
        starterCat.setName("Starters");

        MenuItem biryani = new MenuItem();
        biryani.setId(BIRYANI_ITEM_ID);
        biryani.setName("Chicken Dum Biryani");
        biryani.setPrice(1700L);
        biryani.setAvailable(true);
        CloverList<Category> birCats = new CloverList<>();
        birCats.setElements(List.of(biryaniCat));
        biryani.setCategories(birCats);

        MenuItem starter = new MenuItem();
        starter.setId(STARTER_ITEM_ID);
        starter.setName("Chicken 65");
        starter.setPrice(1200L);
        starter.setAvailable(true);
        CloverList<Category> staCats = new CloverList<>();
        staCats.setElements(List.of(starterCat));
        starter.setCategories(staCats);

        Mockito.when(cloverClient.getAvailableMenuItems()).thenReturn(List.of(biryani, starter));
    }

    @Test
    void fullMenuReturnsCategoriesFromClover() throws Exception {
        mvc.perform(get("/api/menu"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.categories").isArray())
            .andExpect(jsonPath("$.data.categories.length()").value(2))
            .andExpect(jsonPath("$.data.totalItems").value(2));
    }

    @Test
    void categoriesEndpointReturnsSlugs() throws Exception {
        mvc.perform(get("/api/menu/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].slug").value("biryani"));
    }

    @Test
    void itemLookupByIdReturnsItem() throws Exception {
        mvc.perform(get("/api/menu/items/" + BIRYANI_ITEM_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("Chicken Dum Biryani"));
    }

    @Test
    void unknownItemReturns404() throws Exception {
        mvc.perform(get("/api/menu/items/does-not-exist"))
            .andExpect(status().isNotFound());
    }
}
