package com.zbob.config;

import com.zbob.modules.brands.entity.Brand;
import com.zbob.modules.brands.repository.BrandRepository;
import com.zbob.modules.categories.entity.Category;
import com.zbob.modules.categories.repository.CategoryRepository;
import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.products.entity.Product;
import com.zbob.modules.products.entity.ProductBadge;
import com.zbob.modules.products.repository.ProductRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Fills an empty development database with demo products. Never runs in production. */
@Component
@Profile("dev")
public class DevDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private record Item(String name, int category, ProductBadge badge, int price, int was, String description,
            String material, String care) {
    }

    private static final String WARRANTY = "Free repair of any seam fault in the first year";

    private static final List<Item> ITEMS = List.of(
            new Item("Ribbed Merino Crew", 0, ProductBadge.NEW, 6400, 8200, "A fine-gauge crew neck that layers cleanly under a jacket and stands alone on warmer days.", "Extra-fine merino wool", "Hand wash cold, dry flat"),
            new Item("Longline Wool Overcoat", 1, ProductBadge.NEW, 22000, 29000, "A clean, unlined overcoat with a dropped shoulder and a length that works over anything.", "Recycled wool blend", "Dry clean only"),
            new Item("Dungaree Play Set", 2, ProductBadge.NEW, 4800, 6200, "Hard-wearing denim dungarees with adjustable straps that grow with them.", "Soft-washed cotton denim", "Machine wash warm"),
            new Item("Harbour Camp Shirt", 0, ProductBadge.NEW, 5200, 6800, "A relaxed camp collar and an easy, boxy body for the warmest days of the year.", "Washed cotton poplin", "Machine wash cold"),
            new Item("Second Skin Jersey Tee", 1, ProductBadge.NEW, 3800, 4800, "A close-fitting tee in a stretch jersey that keeps its shape through every wash.", "Cotton and elastane jersey", "Machine wash cold"),
            new Item("Field Pocket Cargo", 0, ProductBadge.NEW, 9200, 12000, "Straight-leg cargos with flat pockets that hold their shape without adding bulk.", "Heavy cotton twill", "Machine wash cold"),
            new Item("Everyday Square Tee", 0, ProductBadge.NEW, 3500, 4500, "Our daily tee: a square cut in dense organic cotton that holds its structure.", "Organic combed cotton", "Machine wash cold"),
            new Item("Fold Front Trouser", 1, ProductBadge.NEW, 7800, 10000, "Sharp front pleats and a soft drape take these from the desk to dinner.", "Lyocell and wool blend", "Dry clean recommended"),
            new Item("Loop Back Short", 0, ProductBadge.NEW, 4200, 5500, "Easy drawstring shorts in a soft loopback cotton for rest days and warm evenings.", "Loopback cotton", "Machine wash cold"),
            new Item("Heavy Fleece Hoodie", 0, ProductBadge.BEST_SELLER, 8800, 11000, "A dense, brushed-back hoodie with a generous hood and a slightly oversized fit.", "Brushed organic cotton fleece", "Machine wash cold"),
            new Item("Junior Cable Knit", 2, ProductBadge.BEST_SELLER, 4500, 6000, "A soft cable-knit jumper made for climbing, running and everything in between.", "Cotton and wool blend", "Machine wash wool cycle"),
            new Item("Padded Flight Jacket", 0, ProductBadge.BEST_SELLER, 14500, 18000, "Light padding and ribbed trims give a classic flight jacket a modern, easy shape.", "Recycled polyester shell", "Machine wash cold"),
            new Item("Hooded Down Gilet", 1, ProductBadge.BEST_SELLER, 7500, 9500, "A warm layer without the bulk, with a deep hood and clean quilted channels.", "Responsibly sourced down", "Specialist clean"),
            new Item("Soft Coated Legging", 1, ProductBadge.BEST_SELLER, 7500, 9900, "A sleek coated finish on a stretch base that moves with you all day.", "Coated stretch jersey", "Wipe clean or wash cold"),
            new Item("Short Square Blazer", 1, ProductBadge.BEST_SELLER, 13000, 17500, "A cropped, square-shouldered blazer that sharpens anything underneath it.", "Italian wool blend", "Dry clean only"),
            new Item("Linen Weekend Dress", 1, ProductBadge.NEW, 8600, 11000, "A loose linen dress that gets softer each time you wear it.", "Washed European linen", "Machine wash cold"),
            new Item("Canvas Chore Jacket", 0, ProductBadge.NEW, 11000, 14000, "A structured chore coat with three patch pockets and a soft-washed feel.", "Organic cotton canvas", "Machine wash cold"),
            new Item("Little Puffer Coat", 2, ProductBadge.NEW, 8200, 9000, "A warm, wipe-clean puffer that stands up to winter playgrounds.", "Recycled polyester fill", "Machine wash warm"),
            new Item("Mini Rugby Stripe", 2, ProductBadge.NEW, 3200, 4000, "A sturdy striped rugby top that takes every grass stain in its stride.", "Heavy cotton jersey", "Machine wash warm"),
            new Item("Terry Lounge Set", 2, ProductBadge.NEW, 2500, 3500, "A soft terry top and short set made for lazy mornings at home.", "Cotton terry", "Machine wash warm"));

    private final CategoryRepository categories;
    private final BrandRepository brands;
    private final ProductRepository products;
    private final ProductVariantRepository variants;

    public DevDataSeeder(CategoryRepository categories, BrandRepository brands, ProductRepository products,
            ProductVariantRepository variants) {
        this.categories = categories;
        this.brands = brands;
        this.products = products;
        this.variants = variants;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (categories.count() > 0) {
            return;
        }
        Brand brand = brands.save(new Brand("zbob", "zbob"));
        List<Category> cats = List.of(
                category("Men's Wear", "mens-wear", "New", 0, "Tailored, considered pieces built around how you really dress, from the first coffee to the last train home."),
                category("Women's Wear", "womens-wear", "New", 1, "Soft, easy shapes that move with you and layer without any effort at all, whatever the day brings."),
                category("Children's Wear", "childrens-wear", "New", 2, "Durable, soft-touch clothes made for play, with room to grow and fabric that keeps up with them."));
        for (Item item : ITEMS) {
            Product product = new Product(item.name(), slug(item.name()), cats.get(item.category()));
            product.setDescription(item.description());
            product.setMaterial(item.material());
            product.setCare(item.care());
            product.setWarranty(WARRANTY);
            product.setBadge(item.badge());
            product.setPriceCents(item.price());
            product.setCompareAtPriceCents(item.was());
            product.setPublished(true);
            product.setBrand(brand);
            products.save(product);
            int stock = 8;
            for (String size : List.of("S", "M", "L")) {
                variants.save(new ProductVariant(product, sku(product, size), size, "Black", stock));
                stock += 6;
            }
        }
        log.info("Seeded {} demo products", ITEMS.size());
    }

    private Category category(String name, String slug, String badge, int position, String description) {
        Category category = new Category(name, slug);
        category.setBadge(badge);
        category.setPosition(position);
        category.setDescription(description);
        return categories.save(category);
    }

    private static String slug(String name) {
        return com.zbob.common.util.Slugs.of(name);
    }

    private static String sku(Product product, String size) {
        return "ZB-" + product.getId() + "-BLK-" + size;
    }
}
