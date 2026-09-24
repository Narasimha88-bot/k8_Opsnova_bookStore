# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Build stage - full Maven + JDK 21. Identical to catalog-service.
# ---------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------------------------------------------------------------------------
# Runtime stage - JRE only, Alpine, exact-pinned.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21.0.5_11-jre-alpine

# Teaching convenience: bake in network debug tools (curl, nslookup, dig) so the
# instructor can `kubectl exec` into a pod and demonstrate ClusterIP DNS + the
# order->catalog call live. busybox already provides ping / nslookup / wget.
# Intentional for this teaching image; a real production image would omit these.
RUN apk add --no-cache curl bind-tools

# Non-root user (UID 1000). Same as catalog-service.
RUN addgroup -S -g 1000 opsnova \
 && adduser -S -u 1000 -G opsnova -h /app opsnova

WORKDIR /app
COPY --from=build /build/target/order-service.jar app.jar

USER 1000
EXPOSE 8080

# Version + UI accent baked as env so the 2.0.0 image differs from 1.0.0 ONLY in
# these two values (rolling-update demo). Build 2.0.0 with:
#   docker build --build-arg APP_VERSION=2.0.0 --build-arg APP_ACCENT=#10b981 ...
ARG APP_VERSION=1.0.0
ARG APP_ACCENT=#7c3aed
ENV APP_VERSION=${APP_VERSION} \
    APP_UI_ACCENT=${APP_ACCENT}

# Exec form, no -Xmx (JVM reads cgroup limits itself).
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
