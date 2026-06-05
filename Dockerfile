# Build stage
# Build context must be ./back (set in docker-compose.yml)
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Install parent POM + commons modules to local Maven repository
COPY financial-app-parent financial-app-parent
RUN mvn -f financial-app-parent/pom.xml install -B -q -DskipTests

# Resolve dependencies (cached layer — only re-runs when pom.xml changes)
COPY ms-finances/pom.xml ms-finances/pom.xml
RUN mvn -f ms-finances/pom.xml dependency:resolve -q

# Build
COPY ms-finances/src ms-finances/src
RUN mvn -f ms-finances/pom.xml clean package -DskipTests -q

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/ms-finances/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
