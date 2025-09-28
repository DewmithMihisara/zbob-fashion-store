package com.zbob.support;

import com.zbob.modules.categories.entity.Category;
import com.zbob.modules.categories.repository.CategoryRepository;
import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.products.entity.Product;
import com.zbob.modules.products.repository.ProductRepository;
import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import com.zbob.security.JwtService;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;

/** Creates the rows that cart and order tests need. */
@TestComponent
@Import(TestData.class)
public class TestData {

    private final CategoryRepository categories;
    private final ProductRepository products;
    private final ProductVariantRepository variants;
    private final UserRepository users;
    private final JwtService jwt;

    public TestData(CategoryRepository categories, ProductRepository products, ProductVariantRepository variants,
            UserRepository users, JwtService jwt) {
        this.categories = categories;
        this.products = products;
        this.variants = variants;
        this.users = users;
        this.jwt = jwt;
    }

    public ProductVariant variant(String name, int priceCents, int stock) {
        String slug = name.toLowerCase().replace(' ', '-');
        Category category = categories.findBySlug("test-category")
                .orElseGet(() -> categories.save(new Category("Test Category", "test-category")));
        Product product = new Product(name, slug, category);
        product.setDescription("A description");
        product.setPriceCents(priceCents);
        product.setPublished(true);
        return variants.save(new ProductVariant(products.save(product), slug + "-m", "M", "Black", stock));
    }

    public User customer(String email) {
        return users.save(new User(email, "hash", "Test Customer", Role.CUSTOMER));
    }

    public String bearer(User user) {
        return "Bearer " + jwt.issue(user.getId(), user.getRole());
    }
}
