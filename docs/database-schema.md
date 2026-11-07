# Database schema

MySQL 8, managed by the Flyway migrations in `apps/backend/src/main/resources/db/migration`. Those files are the source of truth.

| Table | Purpose | Key relationships |
|---|---|---|
| `users` | Admins, staff and customers, with a `role` | |
| `categories` | Shop groups, shown as collections | |
| `brands` | Product brands | |
| `products` | Details, prices in cents, publish flag, badge | category, brand |
| `product_images` | Ordered product photos | product (cascade) |
| `product_variants` | Stock per size and colour | product (cascade) |
| `site_media` | One photo per named website slot | |
| `reviews` | Customer reviews awaiting or past approval | product, user (set null) |
| `blog_posts` | Posts; `published_at` is null for drafts | |
| `contact_messages` | Contact form submissions | |
| `newsletter_subscribers` | Footer signups | |
| `customer_addresses` | Saved delivery addresses | user (cascade) |
| `promotions` | Discount codes | |
| `shipping_methods` | Delivery options | |
| `carts`, `cart_items` | One cart per customer | user, variant (cascade) |
| `orders`, `order_items` | Orders, with copies of prices, products and address | user, variant (set null) |
| `payments` | Payment attempts; no card data is ever stored | order (cascade) |

Money is always an integer number of cents. Orders copy the prices, product names and address they were placed with, so later changes never alter past orders.
