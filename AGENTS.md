# open-admin-flowable — Agent Guide

## Project structure
- **Maven multi-module** (Java 21, Spring Boot 4.1.0, Flowable 8.0.0)
  - `open-admin-flowable/` — published to Maven Central
  - `open-admin-flowable-example/` — local dev app, not published
- **Frontend** (`web/`) — UmiJS npm package `@jiangood/open-admin-flowable`

## Version bumping (required before tag)

CI publishes on `v*` tags. Before tagging, update ALL three locations:

1. **Parent POM** — `pom.xml` `<version>`
2. **Frontend** — `web/package.json` `"version"`
3. **Sub-module POMs** — `open-admin-flowable/pom.xml` and `open-admin-flowable-example/pom.xml` inherit via `<parent><version>`, so they auto-update when parent changes — verify they reference the new parent version.

The versions *must* match across all files above.

## Dev commands

```bash
# Backend (full build)
./mvnw clean install -DskipTests -q

# Backend (run example app, port 8082, context-path /process)
./mvnw spring-boot:run -pl open-admin-flowable-example

# Frontend (Umi dev server, proxies backend at 127.0.0.1:8082)
cd web && npm run dev
```

## CI / publish workflow

`.github/workflows/publish.yml` triggers on `v*` tag push. Sequence:
1. `mvn deploy -Dmaven.test.skip=true -pl open-admin-flowable -am -P publish` — only starter module goes to Maven Central
2. `npm publish --access public` from `web/`
3. GitHub Release created automatically

Profile `publish` on starter module activates flatten-maven-plugin (flattenMode=oss), central-publishing, and gpg-signing.

## Frontend notes
- Forms auto-register from `web/src/forms/*.jsx` via the `@jiangood/open-admin/config/common-plugin` Umi plugin
- Build artifacts (`web/src/.umi/`, `web/dist/`) are gitignored
- `web/.env` contains Umi config (proxy, etc.)
