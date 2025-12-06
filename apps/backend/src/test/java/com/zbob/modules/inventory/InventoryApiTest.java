package com.zbob.modules.inventory;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.categories.entity.Category;
import com.zbob.modules.categories.repository.CategoryRepository;
import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.products.entity.Product;
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
@WithMockUser(roles = "STAFF")
class InventoryApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductRepository products;

    @Autowired
    private CategoryRepository categories;

    @Autowired
    private ProductVariantRepository variants;

    private Product product;

    @BeforeEach
    void setUp() {
        Category category = categories.save(new Category("Men's Wear", "mens-wear"));
        Product p = new Product("Boxy Tee", "boxy-tee", category);
        p.setDescription("A description");
        p.setPriceCents(3500);
        p.setPublished(true);
        product = products.save(p);
    }

    private static String variantJson(String sku, String size, int stock) {
        return "{\"sku\":\"%s\",\"size\":\"%s\",\"color\":\"Black\",\"stockQuantity\":%d,\"lowStockThreshold\":5}"
                .formatted(sku, size, stock);
    }

    private String variantsUrl() {
        return "/api/v1/admin/products/" + product.getId() + "/variants";
    }

    @Test
    void addsVariant() throws Exception {
        mvc.perform(post(variantsUrl()).contentType(MediaType.APPLICATION_JSON).content(variantJson("BT-M", "M", 10)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stockQuantity").value(10))
                .andExpect(jsonPath("$.lowStock").value(false));
    }

    @Test
    void rejectsDuplicateSkuAndDuplicateOption() throws Exception {
        variants.save(new ProductVariant(product, "BT-M", "M", "Black", 1));

        mvc.perform(post(variantsUrl()).contentType(MediaType.APPLICATION_JSON).content(variantJson("BT-M", "L", 1)))
                .andExpect(status().isConflict());
        mvc.perform(post(variantsUrl()).contentType(MediaType.APPLICATION_JSON).content(variantJson("BT-M2", "M", 1)))
                .andExpect(status().isConflict());
    }

    @Test
    void negativeStockIsRejected() throws Exception {
        ProductVariant variant = variants.save(new ProductVariant(product, "BT-M", "M", "Black", 1));

        mvc.perform(patch("/api/v1/admin/variants/" + variant.getId() + "/stock")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"stockQuantity\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void settingStockToZeroMakesTheProductOutOfStock() throws Exception {
        ProductVariant variant = variants.save(new ProductVariant(product, "BT-M", "M", "Black", 4));
        mvc.perform(get("/api/v1/products/boxy-tee")).andExpect(jsonPath("$.inStock").value(true));

        mvc.perform(patch("/api/v1/admin/variants/" + variant.getId() + "/stock")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"stockQuantity\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lowStock").value(true));

        variants.flush();
        mvc.perform(get("/api/v1/products/boxy-tee")).andExpect(jsonPath("$.inStock").value(false));
    }

    @Test
    void lowStockFilterListsOnlyLowRows() throws Exception {
        variants.save(new ProductVariant(product, "BT-S", "S", "Black", 2));
        variants.save(new ProductVariant(product, "BT-L", "L", "Black", 50));

        mvc.perform(get("/api/v1/admin/inventory")).andExpect(jsonPath("$.content", hasSize(2)));
        mvc.perform(get("/api/v1/admin/inventory").param("lowStockOnly", "true"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].sku").value("BT-S"))
                .andExpect(jsonPath("$.content[0].productName").value("Boxy Tee"));
    }

    @Test
    void stockListIsSortedByProductThenSize() throws Exception {
        variants.save(new ProductVariant(product, "BT-S", "S", "Black", 9));
        variants.save(new ProductVariant(product, "BT-L", "L", "Black", 9));

        mvc.perform(get("/api/v1/admin/inventory"))
                .andExpect(jsonPath("$.content[0].size").value("L"))
                .andExpect(jsonPath("$.content[1].size").value("S"));
    }

    @Test
    void productVariantsArePaged() throws Exception {
        variants.save(new ProductVariant(product, "BT-L", "L", "Black", 9));
        variants.save(new ProductVariant(product, "BT-S", "S", "Black", 9));

        mvc.perform(get(variantsUrl()).param("size", "1").param("page", "1"))
                .andExpect(jsonPath("$.content[0].size").value("S"))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void variantsOfUnknownProductAreNotFound() throws Exception {
        mvc.perform(get("/api/v1/admin/products/999999/variants")).andExpect(status().isNotFound());
    }

    @Test
    void editsVariantDetails() throws Exception {
        ProductVariant variant = variants.save(new ProductVariant(product, "BT-M", "M", "Black", 1));

        mvc.perform(put("/api/v1/admin/variants/" + variant.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(variantJson("BT-NEW", "XL", 7)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("BT-NEW"))
                .andExpect(jsonPath("$.size").value("XL"))
                .andExpect(jsonPath("$.stockQuantity").value(7));
    }

    @Test
    void deletesVariant() throws Exception {
        ProductVariant variant = variants.save(new ProductVariant(product, "BT-M", "M", "Black", 1));

        mvc.perform(delete("/api/v1/admin/variants/" + variant.getId())).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotChangeStock() throws Exception {
        ProductVariant variant = variants.save(new ProductVariant(product, "BT-M", "M", "Black", 1));

        mvc.perform(patch("/api/v1/admin/variants/" + variant.getId() + "/stock")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"stockQuantity\":100}"))
                .andExpect(status().isForbidden());
    }
}
