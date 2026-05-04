# Repository Guidelines

## Project Structure & Module Organization
`woodlin` is a multi-module Maven project plus a Vue frontend.
- `woodlin-admin`: Spring Boot application entry (`WoodlinAdminApplication`), REST controllers, runtime config.
- `woodlin-common`: shared entities, utilities, config, interceptors, and common tests.
- `woodlin-system/*`: domain modules (core, security, file, datasource, sql2api, task, etl, dsl, generator, tenant).
- `woodlin-web`: Vue 3.5 + TypeScript admin UI. Source under `src/` is split into `api/`, `assets/`, `components/` (global `W*` widgets + `PermissionButton` / `ParentView`), `composables/`, `config/`, `constants/`, `directives/`, `layouts/` (`DefaultLayout` with sidebar/header/tabs/breadcrumb), `locales/` (vue-i18n zh-CN / en-US), `router/` (instance + dynamic routes + guards), `stores/modules/` (Pinia: `user`, `app`, `route`, `tabs`, `permission`, `dict`, `tenant`), `styles/`, `types/`, `utils/`, `views/`.
- `sql/`: schema and seed data for MySQL/PostgreSQL; `docker/` and `docker-compose.yml` for local infra.
- `scripts/`: dev/bootstrap/quality scripts; `docs/` and `documentation/` for architecture and module docs.

## Frontend Conventions
- Component registration is automatic via `unplugin-vue-components` — drop a `.vue` file under `src/components/**/index.vue` and use it directly in templates without manual `import`. Naive UI components are also auto-resolved via `NaiveUiResolver`.
- Icons go through the `WIcon` global component. Use the `vicons:antd:` prefix (or other vicons collection) to render `@vicons/antd` icons by name, e.g. `<WIcon icon="vicons:antd:UserOutlined" />`. The same string format is reused in dynamic menus / route meta.
- Auth flow on app start and after login: `POST /auth/login` → store token in `useUserStore` → `GET /auth/info` to load user/permissions/roles → `GET /auth/routes` to fetch the menu tree → `useRouteStore().generateRoutes()` converts it into Vue Router records and adds them dynamically. The router guard in `src/router/guard.ts` orchestrates this and redirects to `/login` when no token is present.
- i18n uses `vue-i18n`. Inside components call `const { t } = useI18n()` and translate with `t('xxx.yyy')`. Locale files live in `src/locales/{zh-CN,en-US}/` and the active locale is persisted via Pinia + `pinia-plugin-persistedstate`.
- Permission checks: use the `v-permission` directive (e.g. `v-permission="'system:user:add'"`, array form is OR) for elements, or the `<PermissionButton permission="system:user:add">` component for buttons. Both consult `useUserStore().permissions` and short-circuit for super admin (`*:*:*`).

## Build, Test, and Development Commands
- `mvn clean install -DskipTests`: compile all backend modules quickly.
- `mvn spring-boot:run -pl woodlin-admin -Dspring-boot.run.profiles=dev`: run backend locally.
- `cd woodlin-web && npm install --legacy-peer-deps && npm run dev`: install (loose peer deps required by Naive UI / unplugin-*) and start frontend dev server.
- `cd woodlin-web && npm run build`: build frontend production assets.
- `cd woodlin-web && npm run lint` / `npm run type-check`: ESLint + vue-tsc checks.
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
