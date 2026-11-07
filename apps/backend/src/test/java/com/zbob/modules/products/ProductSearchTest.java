package com.zbob.modules.products;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.categories.entity.Category;
import com.zbob.modules.categories.repository.CategoryRepository;
import com.zbob.modules.products.entity.Product;
import com.zbob.modules.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductSearchTest {

    private static final String URL = "/api/v1/products";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductRepository products;

    @Autowired
    private CategoryRepository categories;

    @BeforeEach
    void setUp() {
        Category category = categories.save(new Category("Search Wear", "search-wear"));
        save(category, "Wool Coat", 9000, true);
        save(category, "Cotton Tee", 2000, true);
        save(category, "Linen Shirt", 5000, true);
        save(category, "Hidden Coat", 100, false);
    }

    private void save(Category category, String name, int price, boolean published) {
        Product product = new Product(name, name.toLowerCase().replace(' ', '-'), category);
        product.setDescription("D");
        product.setPriceCents(price);
        product.setPublished(published);
        products.save(product);
    }

    @Test
    void searchMatchesPartOfTheNameAndSkipsDrafts() throws Exception {
        mvc.perform(get(URL).param("search", "coat").param("category", "search-wear"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Wool Coat"));
    }

    @Test
    void sortsByPriceBothWays() throws Exception {
        mvc.perform(get(URL).param("category", "search-wear").param("order", "price-asc"))
                .andExpect(jsonPath("$.content[0].name").value("Cotton Tee"))
                .andExpect(jsonPath("$.content[2].name").value("Wool Coat"));
        mvc.perform(get(URL).param("category", "search-wear").param("order", "price-desc"))
                .andExpect(jsonPath("$.content[0].name").value("Wool Coat"));
    }

    @Test
    void sortsByName() throws Exception {
        mvc.perform(get(URL).param("category", "search-wear").param("order", "name"))
                .andExpect(jsonPath("$.content[0].name").value("Cotton Tee"))
                .andExpect(jsonPath("$.content[1].name").value("Linen Shirt"));
    }

    @Test
    void pagesThroughResults() throws Exception {
        mvc.perform(get(URL).param("category", "search-wear").param("order", "name").param("size", "2").param("page", "1"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Wool Coat"))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void unknownOrderIsABadRequest() throws Exception {
        mvc.perform(get(URL).param("order", "secret-field")).andExpect(status().isBadRequest());
    }

    @Test
    void visitorsCanNotSortByInternalFields() throws Exception {
        mvc.perform(get(URL).param("category", "search-wear").param("sort", "published,desc").param("order", "name"))
                .andExpect(jsonPath("$.content[0].name").value("Cotton Tee"));
    }
}
