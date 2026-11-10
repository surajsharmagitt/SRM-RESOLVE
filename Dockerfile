# Stage 1: Build the application with Maven & OpenJDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace/app

COPY pom.xml .
COPY src src

# Package jar skipping tests for fast production container builds
RUN mvn clean package -DskipTests

# Stage 2: Runtime with local MariaDB (MySQL-compatible) & JRE 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Install mariadb-server, client, and tzdata
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        mariadb-server \
        mariadb-client \
        tzdata && \
    rm -rf /var/lib/apt/lists/*

# Copy the packaged jar from build stage
COPY --from=build /workspace/app/target/srm-resolve-1.0.0.jar app.jar

# Copy and set entrypoint script
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

# Environment settings
ENV TZ=Asia/Kolkata
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["/app/docker-entrypoint.sh"]
