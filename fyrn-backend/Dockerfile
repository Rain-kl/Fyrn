# Build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn -T 1C clean package -DskipTests

# Run stage
FROM amazoncorretto:17-alpine3.21
WORKDIR /app

# Copy the jar file
COPY --from=build /app/fyrn-aggregation/target/fyrn-aggregation.jar app.jar

# Create config directory
RUN mkdir -p /app/config

EXPOSE 45600

# Run with prod profile and config location
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "-Dspring.config.additional-location=file:/app/config/", "app.jar"]

