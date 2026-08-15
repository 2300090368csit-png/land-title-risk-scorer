# Multi-stage build: compile with the full Maven + JDK image, then ship only
# the jar on a slim JRE. Keeps the deployed image small and means the host
# doesn't need Maven or a JDK.

# ---- Stage 1: build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copy the POM first and resolve dependencies as their own layer. Docker caches
# it, so later code-only changes don't re-download the whole dependency tree.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Stage 2: run ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Run as a non-root user rather than root.
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /build/target/*.jar app.jar

# MaxRAMPercentage lets the JVM size its heap from the container's memory limit
# instead of guessing from the host. SerialGC has a smaller footprint than G1,
# which matters on the small instances free tiers hand out.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"

# Documentation only — the app actually binds to $PORT when the host sets it.
EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
