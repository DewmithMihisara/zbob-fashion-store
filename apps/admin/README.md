# zbob admin dashboard

Where staff manage products, photos, stock and content. It runs entirely in the browser and talks to the API, which enforces all permissions.

Start it from the repository root with `pnpm dev:admin`. See `.env.example` for the variables.

The first admin login comes from `ADMIN_EMAIL` and `ADMIN_PASSWORD` in the API's environment. The login lasts until the browser tab is closed.
