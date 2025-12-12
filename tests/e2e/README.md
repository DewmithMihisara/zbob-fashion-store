# End-to-end tests

These drive the real storefront, admin dashboard and API in a browser. They change data, so point them at a throwaway database, never a real one. The run stops straight away if the test admin below can not sign in, which is how it avoids a real store.

To keep your normal servers running, start the test ones on other ports and set `E2E_API_URL`, `E2E_STORE_URL` and `E2E_ADMIN_URL`. The API then also needs `PORT`, `STORAGE_PUBLIC_BASE_URL` and `CORS_ALLOWED_ORIGINS` to match, and the frontends need `API_URL` and `NEXT_PUBLIC_API_URL`.

```sh
# 1. Create an empty database
mysql -u root -p -e "CREATE DATABASE zbob_e2e;"

# 2. Start the API against it, with the first admin the tests sign in as
DB_NAME=zbob_e2e ADMIN_EMAIL=e2e-admin@zbob.test ADMIN_PASSWORD=e2e-admin-password \
  JWT_SECRET=<at least 32 characters> DB_USERNAME=<user> DB_PASSWORD=<password> pnpm dev:backend

# 3. In two more terminals
pnpm dev:storefront
pnpm dev:admin

# 4. Run the tests
pnpm --filter @zbob/e2e exec playwright install chromium
pnpm --filter @zbob/e2e e2e
```

The API must run with the `dev` profile (the default) so the demo products exist.
