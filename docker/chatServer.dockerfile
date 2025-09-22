FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy the parent POM and module POMs first
COPY pom.xml ./
COPY chat-common/pom.xml ./chat-common/
COPY chat-server/pom.xml ./chat-server/
COPY chat-client/pom.xml ./chat-client/

# Download dependencies (this will be cached if dependencies don't change)
RUN mvn dependency:go-offline -B

# Copy the source code
COPY chat-common/src ./chat-common/src
COPY chat-server/src ./chat-server/src

# Build the project
RUN mvn clean compile package install -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the server JAR file from the build stage
COPY --from=build /app/chat-server/target/chat-server-1.0-SNAPSHOT.jar ./

# Create directory for logs
RUN mkdir -p /app/logs && chmod 777 /app/logs

# Set the entrypoint to run the server
ENTRYPOINT ["java", "-jar", "chat-server-1.0-SNAPSHOT.jar"]

# Expose default Spring Boot port
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1