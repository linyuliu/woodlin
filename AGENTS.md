# Repository Guidelines

## Project Structure & Module Organization
`woodlin` is a multi-module Maven project plus a Vue frontend.
- `woodlin-admin`: Spring Boot application entry (`WoodlinAdminApplication`), REST controllers, runtime config.
- `woodlin-common`: shared entities, utilities, config, interceptors, and common tests.
- `woodlin-system/*`: domain modules (core, security, file, datasource, sql2api, task, etl, dsl, generator, tenant).
- `woodlin-web`: Vue 3 + TypeScript admin UI (`src/views`, `src/api`, `src/router`, `src/stores`).
- `sql/`: schema and seed data for MySQL/PostgreSQL; `docker/` and `docker-compose.yml` for local infra.
- `scripts/`: dev/bootstrap/quality scripts; `docs/` and `documentation/` for architecture and module docs.

## Build, Test, and Development Commands
- `mvn clean install -DskipTests`: compile all backend modules quickly.
- `mvn spring-boot:run -pl woodlin-admin -Dspring-boot.run.profiles=dev`: run backend locally.
- `cd woodlin-web && npm install && npm run dev`: start frontend dev server.
- `cd woodlin-web && npm run build`: build frontend production assets.
- `./scripts/quality-check.sh`: run backend + frontend quality checks (Checkstyle, SpotBugs, JavaDoc, tests, ESLint, type-check).
- `./scripts/quick-start.sh start`: launch local stack with Docker Compose.

## Coding Style & Naming Conventions
- Follow `.editorconfig`: Java/XML use 4 spaces; TS/Vue/JSON/YAML/SQL use 2 spaces; LF endings; UTF-8.
- Keep line length at 120 (enforced by Checkstyle for Java).
- Java naming: `PascalCase` classes, `camelCase` methods/fields, `UPPER_SNAKE_CASE` constants, no wildcard imports.
- Public/protected Java APIs should include JavaDoc; Checkstyle validates missing docs and naming patterns.
- Frontend style is enforced by ESLint + Prettier (`npm run lint`, `npm run format`).

## Testing Guidelines
- Backend tests use JUnit 5 (`spring-boot-starter-test`), with Mockito/AssertJ available; some modules include Kotlin/JUnit tests.
- Place tests under `src/test/java` (or `src/test/kotlin`), naming classes `*Test` or `*Tests`.
- Run all tests: `mvn test`; module-only example: `mvn -pl woodlin-system/woodlin-system-core test`.
- Generate coverage report with `mvn test jacoco:report` and review `target/site/jacoco/index.html`.

## Commit & Pull Request Guidelines
- Use Conventional Commit style seen in history: `feat(scope): ...`, `fix(scope): ...`, `refactor(scope): ...`.
- Keep `scope` aligned to module or area (for example `core`, `menu`, `router`, `permission`).
- Create feature branches as documented in `README.md` (for example `feature/AmazingFeature`).
- PRs should include: change summary, affected modules, local test/quality commands run, and screenshots for UI changes.
- Ensure GitHub workflows (`ci.yml`, `code-quality.yml`, `pr-checks.yml`) pass before requesting review.
