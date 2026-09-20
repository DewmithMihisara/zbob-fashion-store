# API

Base path: `/api/v1`. Interactive documentation is at `/swagger-ui.html` when the API runs in development. It is switched off in production.

Authenticated requests send `Authorization: Bearer <token>`.

## Errors

Every error has the same shape:

```json
{ "status": 400, "code": "VALIDATION_FAILED", "message": "Request validation failed", "fieldErrors": { "name": "must not be blank" } }
```

## Public

| Method | Path | Purpose |
|---|---|---|
| GET | `/products` | Published products. Filters: `category`, `badge`, `search`, and `order` (`newest`, `price-asc`, `price-desc` or `name`), plus `page` and `size` |
| GET | `/products/{slug}/reviews` | Approved reviews for one product, with its own score |
| GET | `/products/{slug}` | One published product, with its sizes and colours. Reports availability, never counts |
| GET | `/categories`, `/categories/with-prices` | Categories, optionally with price ranges |
| GET | `/brands` | Brands |
| GET | `/blog`, `/blog/{slug}` | Published posts. Filter: `topic` |
| GET | `/reviews` | Approved reviews with computed score |
| GET | `/site-media` | Slot name to photo URL |
| GET | `/shipping-methods` | Active delivery options |
| POST | `/contact`, `/newsletter` | Contact form and newsletter signup |
| POST | `/stock-alerts` | Ask to be emailed once when a sold out size is back |
| POST | `/auth/customer/register`, `/auth/customer/login`, `/auth/admin/login` | Accounts and sign in |

## Customer (role `CUSTOMER`)

| Method | Path | Purpose |
|---|---|---|
| GET, PUT | `/me` | Profile |
| PUT | `/me/password` | Change password, given the current one |
| GET, POST, PUT, DELETE | `/me/addresses` | Delivery addresses |
| GET, POST, PATCH, DELETE | `/cart`, `/cart/items` | Cart |
| GET, POST | `/orders` | Place and list own orders |
| POST | `/orders/{id}/payments` | Returns `501` until a gateway is added |
| POST | `/checkout/quote` | Prices the cart, or one item with `buyNow`, with delivery and promo code. Saves nothing |
| POST | `/orders/{id}/cancel` | Cancel an order that is not paid yet |
| GET, POST | `/orders/{id}/returns` | Ask to return a delivered order, and see the answer |
| POST | `/reviews` | Submit a review for approval, one per product |
| GET, PUT, DELETE | `/me/wishlist`, `/me/wishlist/ids`, `/me/wishlist/{productId}` | Saved products |

## Admin (roles `STAFF` and `ADMIN`, under `/admin`)

Every list here is paged on the server: use `page` (from 0), `size` (up to 100) and `sort`.

| Path | Purpose |
|---|---|
| `/dashboard` | Summary counts |
| `/products`, `/products/{id}/images`, `/products/{id}/images/order` | Products and photos. A sixth photo returns `409` |
| `/inventory`, `/products/{id}/variants`, `/variants/{id}`, `/variants/{id}/stock` | Stock |
| `/categories`, `/brands` | Catalog groups. Deleting needs `ADMIN` |
| `/site-media`, `/site-media/{slot}` | Website photos |
| `/blog`, `/blog/{id}/cover`, `/reviews` | Content |
| `/orders`, `/orders/{id}/items`, `/orders/{id}/address`, `/orders/{id}/status`, `/orders/{id}/payments` | Orders. Deleting needs `ADMIN` and a cancelled order |
| `/promotions`, `/shipping-methods` | Sales settings |
| `/customers`, `/contact-messages`, `/newsletter-subscribers` | People. Deleting a customer needs `ADMIN` and no orders |
| `/staff`, `/staff/{id}/password` | Staff accounts. Needs `ADMIN` |
| `/inventory/bulk` | Set or adjust stock for up to 200 rows at once, all or nothing |
| `/exports/{products,inventory,customers,orders,subscribers}.csv` | CSV downloads. Customers, orders and subscribers need `ADMIN` |
| `/activity` | Who changed what in the dashboard, and dashboard sign ins. Needs `ADMIN` |
| `/reports/sales?days=` | Orders, revenue, best sellers and stock health for a period |
| `/returns`, `/returns/{id}` | Return requests. Approve, decline, then mark completed |
| `/customers/{id}/password-reset` | Email a customer a reset link |
