package com.zbob.modules.products;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.categories.entity.Category;
import com.zbob.modules.categories.repository.CategoryRepository;
import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.products.entity.Product;
import com.zbob.modules.products.entity.ProductBadge;
import com.zbob.modules.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductApiTest {

    private static final String PUBLIC_URL = "/api/v1/products";
    private static final String ADMIN_URL = "/api/v1/admin/products";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductRepository products;

    @Autowired
    private CategoryRepository categories;

    @Autowired
    private ProductVariantRepository variants;

    private Category mens;

    @BeforeEach
    void setUp() {
        mens = categories.save(new Category("Men's Wear", "mens-wear"));
    }

    private Product saveProduct(String name, boolean published, ProductBadge badge) {
        Product product = new Product(name, name.toLowerCase().replace(' ', '-'), mens);
        product.setDescription("A description");
        product.setPriceCents(3500);
        product.setPublished(published);
        product.setBadge(badge);
        return products.save(product);
    }

    private String body(String name, int price, Integer compareAt) {
        return """
                {"name":"%s","description":"Soft cotton","priceCents":%d,"compareAtPriceCents":%s,
                 "published":true,"categoryId":%d}""".formatted(name, price, compareAt, mens.getId());
    }

    @Test
    void publicListHidesUnpublishedProducts() throws Exception {
        saveProduct("Visible Tee", true, null);
        saveProduct("Draft Tee", false, null);

        mvc.perform(get(PUBLIC_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Visible Tee"));
    }

    @Test
    void publicListFiltersByBadgeAndCategory() throws Exception {
        saveProduct("Seller", true, ProductBadge.BEST_SELLER);
        saveProduct("Fresh", true, ProductBadge.NEW);

        mvc.perform(get(PUBLIC_URL).param("badge", "BEST_SELLER").param("category", "mens-wear"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Seller"));
        mvc.perform(get(PUBLIC_URL).param("category", "womens-wear"))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void productWithoutImageHasNullUrlForThePlaceholder() throws Exception {
        saveProduct("Plain Tee", true, null);

        mvc.perform(get(PUBLIC_URL)).andExpect(jsonPath("$.content[0].imageUrl").value(nullValue()));
    }

    @Test
    void inStockFollowsVariantQuantities() throws Exception {
        Product product = saveProduct("Stocked Tee", true, null);
        ProductVariant variant = variants.save(new ProductVariant(product, "ST-M", "M", "Black", 0));

        mvc.perform(get(PUBLIC_URL + "/stocked-tee")).andExpect(jsonPath("$.inStock").value(false));

        variant.setStockQuantity(3);
        variants.flush();
        mvc.perform(get(PUBLIC_URL + "/stocked-tee")).andExpect(jsonPath("$.inStock").value(true));
    }

    @Test
    void publicProductNeverExposesStockCounts() throws Exception {
        Product product = saveProduct("Secret Tee", true, null);
        variants.save(new ProductVariant(product, "SE-M", "M", "Black", 7));

        mvc.perform(get(PUBLIC_URL + "/secret-tee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variants").doesNotExist())
                .andExpect(jsonPath("$.published").doesNotExist());
    }

    @Test
    void unpublishedProductPageIsNotFound() throws Exception {
        saveProduct("Draft Tee", false, null);

        mvc.perform(get(PUBLIC_URL + "/draft-tee")).andExpect(status().isNotFound());
    }

    @Test
    void anonymousCannotUseAdminApi() throws Exception {
        mvc.perform(get(ADMIN_URL)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotUseAdminApi() throws Exception {
        mvc.perform(get(ADMIN_URL)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void adminListIncludesDraftsAndSearches() throws Exception {
        saveProduct("Draft Coat", false, null);
        saveProduct("Live Tee", true, null);

        mvc.perform(get(ADMIN_URL)).andExpect(jsonPath("$.content", hasSize(2)));
        mvc.perform(get(ADMIN_URL).param("search", "COAT"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Draft Coat"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void searchTreatsWildcardsAsPlainText() throws Exception {
        saveProduct("Live Tee", true, null);

        mvc.perform(get(ADMIN_URL).param("search", "%")).andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void createGeneratesSlug() throws Exception {
        mvc.perform(post(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content(body("Boxy Tee", 3500, 4500)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("boxy-tee"))
                .andExpect(jsonPath("$.category.slug").value("mens-wear"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void originalPriceBelowSalePriceIsRejected() throws Exception {
        mvc.perform(post(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content(body("Odd Tee", 5000, 4000)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void renameKeepsSlug() throws Exception {
        Product product = saveProduct("Old Name", true, null);

        mvc.perform(put(ADMIN_URL + "/" + product.getId()).contentType(MediaType.APPLICATION_JSON).content(body("New Name", 3500, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.slug").value("old-name"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCannotDeleteProducts() throws Exception {
        Product product = saveProduct("Keep Tee", true, null);

        mvc.perform(delete(ADMIN_URL + "/" + product.getId())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void categoryWithProductsCannotBeDeleted() throws Exception {
        saveProduct("Keep Tee", true, null);

        mvc.perform(delete("/api/v1/admin/categories/" + mens.getId())).andExpect(status().isConflict());
    }
}
