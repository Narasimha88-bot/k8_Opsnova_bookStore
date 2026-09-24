# OpsNova — Kubernetes Teaching Application

A deliberately simple **DevOps tools & technologies catalog** (each item is one
tool — Docker, Kubernetes, Terraform, …, shown with its real logo), built to
teach Kubernetes objects one session at a time. It is **not** a product — its
value is how cleanly it demonstrates Kubernetes behaviour. See the developer
specification for the full rationale.

> The internal entity/API stays `Book` / `/api/books` (the spec's contract);
> only the content and UI are themed as tools.

> **Design rule:** boring, predictable Java is correct. The same image runs in
> three modes (`standalone`, `external-db`, `full`) by changing only the
> `SPRING_PROFILES_ACTIVE` environment variable.

---

## Build status

| Stage | Description | Status |
|---|---|---|
| **1** | Skeleton + `standalone` `catalog-service` | ✅ **Done** |
| **2** | Thymeleaf UI (tools catalog, detail, pod/version/profile footer) | ✅ **Done** |
| **3** | `order-service` + inter-service calls (clean 503 when catalog down) | ✅ **Done** |
| **4** | PostgreSQL + `external-db` profile (persistent, `docker-compose.yml`) | ✅ **Done** |
| **5** | Security + JWT + Redis (`full` profile, login page) | ✅ **Done** |
| **6** | Teaching hooks + hardening (`2.0.0` image, read-only fs, prometheus) | ✅ **Done** |

---

## Repository layout

```
k8_application/
├── README.md
├── .gitignore
├── catalog-service/          # Stages 1-2
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/ ...
└── order-service/            # Stage 3 (structurally identical to catalog)
    ├── pom.xml
    ├── Dockerfile
    └── src/main/
        ├── java/tech/opsnova/catalog/
        │   ├── CatalogApplication.java
        │   ├── controller/   # BookController, InfoController, error handler (REST/JSON)
        │   ├── web/          # BookViewController (Thymeleaf pages)
        │   ├── info/         # PodInfo (shared source for /api/info + footer)
        │   ├── service/      # BookService
        │   ├── repository/   # BookRepository (Spring Data JPA)
        │   ├── model/        # Book entity
        │   ├── dto/          # InfoResponse, StockResponse, ErrorResponse
        │   ├── config/       # DataSeeder
        │   └── exception/    # BookNotFoundException
        └── resources/
            ├── application.yml
            ├── application-standalone.yml
            ├── templates/    # fragments, books, book-detail, error (Thymeleaf)
            └── static/       # css/style.css, js/app.js
```

`order-service` arrives in Stage 3; the remaining profile config files arrive in
Stages 4–5. This keeps each stage independently reviewable.

---

## Running catalog-service (Stage 1, `standalone`)

### Option A — Docker (this is the Stage 1 acceptance test)

```bash
docker build -t catalog-service:1.0.0 catalog-service
docker run --rm -p 8080:8080 catalog-service:1.0.0
```

Read-only root filesystem is supported (verified fully in Stage 6):

```bash
docker run --rm -p 8080:8080 --read-only --tmpfs /tmp catalog-service:1.0.0
```

### Option B — Maven (requires JDK 21 + Maven locally)

```bash
cd catalog-service
mvn spring-boot:run
```

### Verify

```bash
curl http://localhost:8080/api/books            # 10 seeded books
curl http://localhost:8080/api/books/1          # single book
curl http://localhost:8080/api/books/1/stock    # stock count
curl http://localhost:8080/api/info             # pod/version/profile
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
```

### The browser UI (Stage 2)

Open **http://localhost:8080/** — it redirects to the catalog. Pages:

| Path | Page |
|---|---|
| `/books` | Tools catalog (real tool logos, live search, stock pills) |
| `/books/{id}` | Tool detail (category, licence, price, stock, order box) |

Every page carries a footer showing **pod name · version · profile** — the
most-used feature in class. Outside Kubernetes the pod name shows `unknown`.
The header shows the version badge (`v1.0.0`) and the active profile. The UI
accent colour comes from `app.ui.accent`, so the `2.0.0` image re-themes by
changing one value.

`/api/info` outside Kubernetes returns `"unknown"` for pod fields — that is
expected and is itself part of the teaching material:

```json
{ "podName": "unknown", "podIp": "unknown", "nodeName": "unknown",
  "version": "1.0.0", "activeProfile": "standalone" }
```

---

## Running the secured `full` profile (Stage 5)

Postgres + Redis + Spring Security + JWT, in one command:

```bash
docker compose -f docker-compose.full.yml up --build
```
(Bring the external-db stack down first: `docker compose down`.)

- Catalog → http://localhost:8080/ · Orders → http://localhost:8081/orders (both `full`)
- **Log in** (top-right) with `admin / admin123` or `user / user123`.
- Browsing books is public; **placing an order requires login**. The JWT rides in
  the `OPSNOVA_TOKEN` cookie (shared across ports on `localhost`), so logging in on
  catalog also authenticates you on order-service.

**API usage:**
```bash
# get a token
curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
# place an order with it (order-service validates the JWT locally)
curl -X POST localhost:8081/api/orders -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" -d '{"bookId":4,"quantity":1}'
```

**Acceptance demos:**
- **Kill Redis** (`docker compose -f docker-compose.full.yml stop redis`) → login and
  ordering still work; only token revocation is disabled (a warning is logged);
  readiness stays UP.
- **Rotate the key on catalog only** → order-service (still on the old key) rejects
  catalog's newly-signed tokens with 401.

`full` env vars (secrets have no defaults — from ConfigMap/Secret in class):

| Variable | Service | Notes |
|---|---|---|
| `JWT_SIGNING_KEY` | both | Secret. No default; ≥ 32 chars (HS256). |
| `SPRING_REDIS_HOST` / `SPRING_REDIS_PORT` | catalog | ConfigMap. Redis for token revocation. |
| `SPRING_DATASOURCE_*` | both | Postgres (as in external-db). |

## Running with PostgreSQL — the `external-db` profile (Stage 4)

One command brings up Postgres + both services on the `external-db` profile:

```bash
docker compose up --build
```

- Catalog → http://localhost:8080/ · Orders → http://localhost:8081/orders
- `/api/info` now reports `"activeProfile":"external-db"`.
- Both services point at ONE Postgres and share nothing but the database
  (catalog owns `books`, order owns `orders`).

**Persistence check (the Stage 4 acceptance):**
```bash
docker compose restart postgres     # or: docker compose stop postgres && docker compose start postgres
# books + orders are still there afterwards - the named volume `pgdata` survives.
```
`docker compose down` keeps the volume (data persists); `docker compose down -v` deletes it.

**Fail-fast check:** with `external-db` active, if Postgres is unreachable (or the
`SPRING_DATASOURCE_*` vars are unset) the service **fails to start on purpose** —
that is what drives the initContainer lesson.

> Stop the plain `docker run` containers first (`docker rm -f catalog-service order-service`)
> so compose can bind ports 8080/8081 and reuse the names.

`external-db` env vars (no defaults — sourced from ConfigMap/Secret in class):

| Variable | Example |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `external-db` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/opsnova` |
| `SPRING_DATASOURCE_USERNAME` | `opsnova` |
| `SPRING_DATASOURCE_PASSWORD` | `opsnova_secret` |

## Running both services on H2 (Stage 3, `standalone`)

order-service calls catalog over HTTP using `CATALOG_SERVICE_URL`. Locally, put
both on a user-defined Docker network so the name `catalog-service` resolves:

```bash
docker network create opsnova
docker run -d --name catalog-service --network opsnova -p 8080:8080 catalog-service:1.0.0
docker run -d --name order-service   --network opsnova -p 8081:8080 \
  -e CATALOG_SERVICE_URL=http://catalog-service:8080 order-service:1.0.0
```

- catalog UI → http://localhost:8080/ · order UI → http://localhost:8081/orders
- Place an order: `curl -X POST localhost:8081/api/orders -H 'Content-Type: application/json' -d '{"bookId":4,"quantity":2}'`

**The hard requirement — catalog down still gives a clean 503:**

```bash
docker stop catalog-service
curl -i -X POST localhost:8081/api/orders -H 'Content-Type: application/json' -d '{"bookId":4,"quantity":1}'
#   HTTP/1.1 503 ... {"status":503,"error":"Service Unavailable",
#                     "message":"catalog-service unreachable at http://catalog-service:8080", ...}
curl -s localhost:8081/actuator/health/readiness   # still UP - order-service never went unready
```

## Endpoints (order-service)

| Method | Path | Description |
|---|---|---|
| POST | `/api/orders` | Place order (verifies stock with catalog; 503 if catalog down) |
| GET | `/api/orders` | List orders (`?username=` optional) |
| GET | `/api/orders/{id}` | Single order |
| GET | `/api/info` | Pod / version / profile (same shape as catalog) |
| GET | `/orders` | Order-history UI + place-order form |

## Endpoints (catalog-service)

| Method | Path | Description |
|---|---|---|
| GET | `/api/books` | List all books |
| GET | `/api/books/{id}` | Single book (404 if missing) |
| POST | `/api/books` | Create a book |
| PUT | `/api/books/{id}` | Update a book |
| DELETE | `/api/books/{id}` | Delete a book |
| GET | `/api/books/{id}/stock` | Stock count (used by order-service) |
| GET | `/api/info` | Pod name, IP, node, version, active profile |
| GET | `/actuator/health/liveness` | Process alive (does NOT check the DB) |
| GET | `/actuator/health/readiness` | Ready for traffic (checks the DB) |
| GET | `/actuator/health` | Full health detail |

Auth columns and the `full`-profile behaviour are added in Stage 5.

---

## Environment variables in play this stage

| Variable | Purpose | Default |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Selects the profile | `standalone` |
| `POD_NAME` | Downward API — pod name in `/api/info` | `unknown` |
| `POD_IP` | Downward API — pod IP in `/api/info` | `unknown` |
| `NODE_NAME` | Downward API — node name in `/api/info` | `unknown` |

The complete variable list (Postgres, Redis, JWT, startup delay) is documented as
those stages land, per the spec's Section 11.

---

## Teaching hooks (Stage 6) — deliberately unsafe, do NOT secure them

All three are **open in every profile** (no auth) and present on **both** services.

| Hook | What it does | Demo |
|---|---|---|
| `POST /api/admin/toggle-readiness` | Flips readiness DOWN/UP in memory (liveness stays UP) | Pod pulled from the Service endpoints without being killed |
| `app.startup.delay.seconds` (env `APP_STARTUP_DELAY_SECONDS`, default 0) | Sleeps before the app marks itself started; readiness stays DOWN for the delay | startupProbe |
| `GET /api/admin/consume-memory?mb=N` | Allocates + retains N MB | Force an OOMKill against the container memory limit |

```bash
curl -X POST localhost:8080/api/admin/toggle-readiness      # -> readiness DOWN, then run again to restore
curl "localhost:8080/api/admin/consume-memory?mb=200"       # allocate 200 MB (repeat to OOMKill)
docker run -e APP_STARTUP_DELAY_SECONDS=20 ...               # readiness DOWN for ~20s at boot
```

`/actuator/prometheus` is exposed on both services for the HPA session.

## Read-only root filesystem

Both images run non-root (UID 1000) and never write outside `/tmp`:

```bash
docker run --rm -p 8080:8080 --read-only --tmpfs /tmp catalog-service:1.0.0
```

## Two image tags — the rolling-update demo

`1.0.0` and `2.0.0` differ **only** in the version string and the UI accent colour
(2.0.0 is emerald). Same code; the values are baked as env via build args:

```bash
docker build -t catalog-service:1.0.0 ./catalog-service
docker build --build-arg APP_VERSION=2.0.0 --build-arg "APP_ACCENT=#10b981" \
  -t catalog-service:2.0.0 ./catalog-service
```
Deploy `:1.0.0`, then roll to `:2.0.0` and watch the footer version + accent change.

## Environment variables — complete list

| Variable | Service | Profile | Source in class |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | both | all | ConfigMap |
| `SPRING_DATASOURCE_URL` | both | external-db, full | ConfigMap |
| `SPRING_DATASOURCE_USERNAME` | both | external-db, full | Secret |
| `SPRING_DATASOURCE_PASSWORD` | both | external-db, full | Secret |
| `JWT_SIGNING_KEY` | both | full | Secret (no default, ≥32 chars) |
| `SPRING_REDIS_HOST` | catalog | full | ConfigMap |
| `SPRING_REDIS_PORT` | catalog | full | ConfigMap |
| `CATALOG_SERVICE_URL` | order | all | ConfigMap |
| `APP_STARTUP_DELAY_SECONDS` | both | all | ConfigMap |
| `APP_UI_ACCENT` | both | all | ConfigMap (baked per image tag) |
| `APP_VERSION` | both | all | baked per image tag |
| `APP_ORDER_URL` | catalog | all | ConfigMap (browser link to order UI) |
| `APP_LOGIN_URL` / `APP_LOGOUT_URL` | order | full | ConfigMap (link to catalog auth) |
| `POD_NAME` / `POD_IP` / `NODE_NAME` | both | all | Downward API |

## Versions (pinned)

| Thing | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.5 |
| Maven build image | `maven:3.9.9-eclipse-temurin-21` |
| Runtime base image | `eclipse-temurin:21.0.5_11-jre-alpine` |

All library versions are pinned transitively through the pinned Spring Boot
parent — no `latest` tags anywhere.
