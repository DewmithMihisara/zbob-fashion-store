# End-to-end tests

These drive the real storefront, admin dashboard and API in a browser. They change data, so point them at a throwaway database, never a real one.

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
