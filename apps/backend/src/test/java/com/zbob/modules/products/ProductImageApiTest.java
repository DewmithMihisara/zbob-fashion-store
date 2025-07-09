package com.zbob.modules.products;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.categories.entity.Category;
import com.zbob.modules.categories.repository.CategoryRepository;
import com.zbob.modules.products.entity.Product;
import com.zbob.modules.products.entity.ProductImage;
import com.zbob.modules.products.repository.ProductImageRepository;
import com.zbob.modules.products.repository.ProductRepository;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "STAFF")
class ProductImageApiTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductRepository products;

    @Autowired
    private CategoryRepository categories;

    @Autowired
    private ProductImageRepository images;

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

    private String url() {
        return "/api/v1/admin/products/" + product.getId() + "/images";
    }

    private static MockMultipartFile file(String name, String type, byte[] content) {
        return new MockMultipartFile("file", name, type, content);
    }

    @Test
    void uploadReplacesThePlaceholderOnTheStorefront() throws Exception {
        mvc.perform(get("/api/v1/products")).andExpect(jsonPath("$.content[0].imageUrl").doesNotExist());

        mvc.perform(multipart(url()).file(file("tee.png", "image/png", PNG)).param("altText", "Front"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.altText").value("Front"))
                .andExpect(jsonPath("$.position").value(0))
                .andExpect(jsonPath("$.url", startsWith("http://localhost:8080/uploads/products/" + product.getId())));

        mvc.perform(get("/api/v1/products"))
                .andExpect(jsonPath("$.content[0].imageUrl", startsWith("http://localhost:8080/uploads/")));
    }

    @Test
    void contentIsCheckedNotTheDeclaredType() throws Exception {
        byte[] script = "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8);

        mvc.perform(multipart(url()).file(file("tee.png", "image/png", script)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only JPEG, PNG and WebP images are allowed"));
    }

    @Test
    void clientFilenameIsNotUsedInTheStoragePath() throws Exception {
        mvc.perform(multipart(url()).file(file("../../evil.png", "image/png", PNG)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url", org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("evil"))));
    }

    @Test
    void reordersAndChangesTheCardImage() throws Exception {
        ProductImage first = images.save(new ProductImage(product, "a.png", "http://x/a.png", null, 0));
        ProductImage second = images.save(new ProductImage(product, "b.png", "http://x/b.png", null, 1));

        mvc.perform(put(url() + "/order").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageIds\":[%d,%d]}".formatted(second.getId(), first.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(second.getId()));

        images.flush();
        mvc.perform(get("/api/v1/products")).andExpect(jsonPath("$.content[0].imageUrl").value("http://x/b.png"));
    }

    @Test
    void reorderMustListEveryImageExactlyOnce() throws Exception {
        ProductImage first = images.save(new ProductImage(product, "a.png", "http://x/a.png", null, 0));
        images.save(new ProductImage(product, "b.png", "http://x/b.png", null, 1));

        mvc.perform(put(url() + "/order").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageIds\":[%d,%d]}".formatted(first.getId(), first.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletesImage() throws Exception {
        ProductImage image = images.save(new ProductImage(product, "a.png", "http://x/a.png", null, 0));

        mvc.perform(delete(url() + "/" + image.getId())).andExpect(status().isNoContent());
        images.flush();

        mvc.perform(get("/api/v1/products/boxy-tee")).andExpect(jsonPath("$.images", hasSize(0)));
    }

    @Test
    void cannotDeleteAnotherProductsImage() throws Exception {
        ProductImage image = images.save(new ProductImage(product, "a.png", "http://x/a.png", null, 0));

        mvc.perform(delete("/api/v1/admin/products/999999/images/" + image.getId())).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotUpload() throws Exception {
        mvc.perform(multipart(url()).file(file("tee.png", "image/png", PNG))).andExpect(status().isForbidden());
    }
}
