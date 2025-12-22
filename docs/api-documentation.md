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
| GET | `/products` | Published products. Filters: `category`, `badge`, plus `page`, `size`, `sort` |
| GET | `/products/{slug}` | One published product, with its sizes and colours. Reports availability, never counts |
| GET | `/categories`, `/categories/with-prices` | Categories, optionally with price ranges |
| GET | `/brands` | Brands |
| GET | `/blog`, `/blog/{slug}` | Published posts. Filter: `topic` |
| GET | `/reviews` | Approved reviews with computed score |
| GET | `/site-media` | Slot name to photo URL |
| GET | `/shipping-methods` | Active delivery options |
| POST | `/contact`, `/newsletter` | Contact form and newsletter signup |
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
| POST | `/reviews` | Submit a review for approval |

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
