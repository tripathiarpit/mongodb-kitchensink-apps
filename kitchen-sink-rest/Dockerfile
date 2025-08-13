## Stage 1: Build the Spring Boot app using Java 21
#FROM eclipse-temurin:21-jdk AS builder
#
#WORKDIR /app
#
## Copy Maven wrapper and pom.xml first
#COPY pom.xml mvnw ./
#COPY .mvn .mvn
#
## Download dependencies
#RUN ./mvnw dependency:go-offline
#
## Copy source code
#COPY src src
#
## Build application
#RUN ./mvnw clean package -DskipTests
#
## Stage 2: Run the app
#FROM eclipse-temurin:21-jdk
#
#WORKDIR /app
#
#COPY --from=builder /app/target/*.jar app.jar
#
#ENTRYPOINT ["java", "-jar", "app.jar"]



## Multi-stage build for Spring Boot application
#FROM maven:3.9-eclipse-temurin-21 AS build
#
## Set working directory
#WORKDIR /app
#
## Copy pom.xml and download dependencies
#COPY pom.xml .
#RUN mvn dependency:go-offline -B
#
## Copy source code and build application
#COPY src ./src
#RUN mvn clean package -DskipTests
#
## Production stage
#FROM eclipse-temurin:21-jdk AS builder
#
## Install curl for health checks
#RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
#
## Set working directory
#WORKDIR /app
#
## Copy the built jar from build stage
#COPY --from=build /app/target/*.jar app.jar
#
## Create non-root user
#RUN addgroup --system spring && adduser --system --group spring
#USER spring:spring
#
## Expose port
#EXPOSE 8080
#
## Health check
#HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
#  CMD curl -f http://localhost:8080/actuator/health || exit 1
#
## Run the application
#ENTRYPOINT ["java", "-jar", "app.jar"]
