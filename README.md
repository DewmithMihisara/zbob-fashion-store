# zbob

Clothing store monorepo: a customer storefront, an admin dashboard and one REST API.

| App | Path | Stack |
|---|---|---|
| Storefront | `apps/storefront` | Next.js, TypeScript |
| Admin dashboard | `apps/admin` | Next.js, TypeScript |
| Backend API | `apps/backend` | Java 21, Spring Boot, MySQL |

Shared TypeScript code lives in `packages/`.

## Requirements

- Node 24 and pnpm (`corepack enable pnpm`)
- Java 21 and Maven 3.9
- MySQL 8

## Commands

```sh
pnpm install
pnpm dev:storefront
pnpm dev:admin
pnpm dev:backend
```
