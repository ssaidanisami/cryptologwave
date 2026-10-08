# -----------------------------------------------------------------------------
# Stage 1: Build Application with Maven
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy Maven wrapper configuration and dependencies descriptor
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies in offline mode for build layer caching
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B || true

# Copy application source tree
COPY src ./src

# Build production executable JAR without running test suites inside container
RUN ./mvnw clean package -DskipTests

# -----------------------------------------------------------------------------
# Stage 2: Lightweight Runtime Image
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-privileged user and group for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled jar from builder stage
COPY --from=builder /workspace/target/cryptolog-wave-*.jar /app/app.jar

# Set ownership to non-root user
RUN chown -R appuser:appgroup /app

USER appuser:appgroup

# Environment defaults
ENV SPRING_PROFILES_ACTIVE=dev \
    PORT=8082 \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

EXPOSE 8082

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
