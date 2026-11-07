# zbob

Clothing store monorepo: a customer storefront, an admin dashboard and one REST API.

| App | Path | Stack | Local URL |
|---|---|---|---|
| Storefront | `apps/storefront` | Next.js, TypeScript | http://localhost:3000 |
| Admin dashboard | `apps/admin` | Next.js, TypeScript | http://localhost:3001 |
| Backend API | `apps/backend` | Java 21, Spring Boot, MySQL | http://localhost:8080 |

Shared TypeScript code lives in `packages/`.

## Requirements

- Node 24 and pnpm (`corepack enable pnpm`)
- Java 21 and Maven 3.9
- MySQL 8

## Getting started

1. Create a database:

   ```sql
   CREATE DATABASE zbob;
   ```

2. Create `apps/backend/.env` from `apps/backend/.env.example`. Set `DB_USERNAME`, `DB_PASSWORD`, a `JWT_SECRET` of at least 32 characters, and `ADMIN_EMAIL` and `ADMIN_PASSWORD` for your first dashboard login. This file is never committed.

3. Install and start everything, each in its own terminal:

   ```sh
   pnpm install
   pnpm dev:backend
   pnpm dev:storefront
   pnpm dev:admin
   ```

   `pnpm dev:backend` reads `apps/backend/.env` for you. In development, an empty database is filled with demo products and sample content.

## Tests

```sh
pnpm -r lint
pnpm -r typecheck
pnpm -r test
mvn -f apps/backend/pom.xml verify
```

Backend tests use an in-memory database by default. To run them on MySQL, set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`. Browser tests are described in [tests/e2e/README.md](tests/e2e/README.md).

## Documentation

- [Architecture](docs/architecture.md)
- [Database schema](docs/database-schema.md)
- [API](docs/api-documentation.md)
- [Deployment guide](docs/deployment-guide.md)
