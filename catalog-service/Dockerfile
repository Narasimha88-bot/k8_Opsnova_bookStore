# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Build stage - full Maven + JDK 21. Nothing from here ends up in the runtime
# image, so the image stays small.
# ---------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy the POM first and pre-fetch dependencies so this layer is cached and
# only re-runs when the POM changes (faster rebuilds during class prep).
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Now copy the source and build the executable jar.
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------------------------------------------------------------------------
# Runtime stage - JRE only, Alpine, exact-pinned. Target image size < 250 MB.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21.0.5_11-jre-alpine

# Teaching convenience: bake in network debug tools so the instructor can
# `kubectl exec` into a pod and demonstrate ClusterIP DNS + service connectivity
# (curl, nslookup, dig) live, without `apk add` each time. busybox already
# provides ping / nslookup / wget. Intentional for this teaching image; a real
# production image would omit these.
RUN apk add --no-cache curl bind-tools

# Non-root user (UID 1000). Required for the SecurityContext session and for the
# read-only root filesystem session (the app never writes outside /tmp).
RUN addgroup -S -g 1000 opsnova \
 && adduser -S -u 1000 -G opsnova -h /app opsnova

WORKDIR /app
COPY --from=build /build/target/catalog-service.jar app.jar

USER 1000
EXPOSE 8080

# Version + UI accent are baked as env so the 2.0.0 image differs from 1.0.0 ONLY
# in these two values (the rolling-update demo). Build 2.0.0 with:
#   docker build --build-arg APP_VERSION=2.0.0 --build-arg APP_ACCENT=#10b981 ...
ARG APP_VERSION=1.0.0
ARG APP_ACCENT=#7c3aed
ENV APP_VERSION=${APP_VERSION} \
    APP_UI_ACCENT=${APP_ACCENT}

# Exec form (no shell wrapper). No -Xmx: the JVM reads cgroup limits itself
# (UseContainerSupport is on by default in Java 21) - this is a deliberate lesson.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
