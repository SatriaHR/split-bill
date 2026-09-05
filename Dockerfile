# Multi-stage build template for Allo Bank Backend Challenge
# Candidates must include a Dockerfile in the root of their project.
# You may modify this template, but the final image must:
#   - Build the application using Maven
#   - Run on port 4110
#   - Start without requiring any manual steps

# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom first (layer cache for dependencies)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q

# Copy source and build
COPY src/ src/
RUN ./mvnw package -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 4110

ENTRYPOINT ["java", "-jar", "app.jar"]



###############
# FROM maven:3.9.11-eclipse-temurin-17 AS build
# WORKDIR /workspace
# COPY pom.xml .
# RUN mvn -B dependency:go-offline
# COPY src ./src
# RUN mvn -B clean package -DskipTests

# FROM eclipse-temurin:17-jre
# WORKDIR /app
# RUN useradd --system --uid 1001 spring
# COPY --from=build /workspace/target/split-bill-api-0.0.1-SNAPSHOT.jar app.jar
# USER 1001
# EXPOSE 8080
# ENTRYPOINT ["java", "-jar", "app.jar"]
