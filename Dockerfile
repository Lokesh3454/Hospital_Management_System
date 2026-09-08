# ===================================================================
# MedPulse HMS - Multi-Stage Unified Dockerfile (Frontend + Backend)
# ===================================================================

# Stage 1: Build Angular 18 Frontend
FROM node:20-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend ./
RUN npm run build

# Stage 2: Build Spring Boot 3.3 Backend with Embedded Frontend Static Assets
FROM maven:3.9.6-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
# Embed compiled Angular production build into Spring Boot classpath static folder
COPY --from=frontend-build /frontend/dist/frontend ./src/main/resources/static
RUN mvn clean package -DskipTests

# Stage 3: Lightweight Alpine JRE 21 Production Container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
