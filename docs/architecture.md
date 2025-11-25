# Architecture

One repository, three independently deployed apps, one API and one database.

```text
 Customers               Staff
     |                     |
 Storefront          Admin dashboard
 (Next.js)             (Next.js)
     |                     |
     +---------+-----------+
               |  REST + JWT
          Backend API
          (Spring Boot)
          |          |
        MySQL     Image storage
                  (disk in dev, Google Cloud Storage in prod)
```

## Rules

- Only the API talks to MySQL. Neither frontend connects to it.
- The API enforces every permission. Hiding a link in the dashboard is a convenience, not security.
- Every schema change is a new Flyway migration. Never edit one that has already run.
- Images live in storage. The database keeps only their URLs and storage keys.

## Roles

| Role | Can do |
|---|---|
| `CUSTOMER` | Own profile, addresses, cart, orders and reviews |
| `STAFF` | Manage catalog, stock, photos, content, orders and customers |
| `ADMIN` | Everything staff can, plus deleting products, categories and brands, and managing staff |

Admins and customers sign in at separate endpoints. An account's role is read from the database on every request, so disabling or demoting someone takes effect immediately rather than when their token expires.

## How photos work

There are two kinds, both uploaded in the dashboard:

- **Product photos** belong to a product (`product_images`). The first is shown on product cards.
- **Site photos** fill named slots such as `home-hero-1` (`site_media`). `SiteMediaSlots` lists every slot.

Anything without a photo shows a neutral placeholder on the storefront. Uploads are identified from the file's contents, not its name, and are stored under a generated name.

## How stock works

Each size and colour of a product is a `product_variants` row with its own count. A product is in stock when any of its variants has stock above 0. The public API reports only that yes or no, never the counts.

## Order Now and payments

Order Now on the product page deliberately does nothing, because no payment gateway has been chosen. See `OrderNowButton.tsx`.

The API for carts, orders and payments exists and is tested, but no storefront page uses it yet. To go live with payments:

1. Implement `PaymentGateway` in the backend for the chosen provider. Until one exists, starting a payment returns `501 PAYMENTS_NOT_CONFIGURED`.
2. Build the size picker, cart and checkout pages on the storefront.
3. Connect `OrderNowButton` to them.
