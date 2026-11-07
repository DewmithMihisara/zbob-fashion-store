# zbob backend

Spring Boot REST API. Modules live in `src/main/java/com/zbob/modules`, each split into controller, service, repository, entity and dto packages where it needs them.

Start it from the repository root with `pnpm dev:backend`, which reads `.env` in this folder. See `.env.example` for the variables.

Swagger UI is at http://localhost:8080/swagger-ui.html in development.

Schema changes go in a new file in `src/main/resources/db/migration`.
