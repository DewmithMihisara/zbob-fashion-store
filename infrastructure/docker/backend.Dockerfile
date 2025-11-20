# Build from the repository root:
#   docker build -f infrastructure/docker/backend.Dockerfile -t zbob-backend .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY apps/backend/pom.xml .
RUN mvn -q -B dependency:go-offline
COPY apps/backend/src ./src
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:21-jre
RUN useradd --system --no-create-home zbob
WORKDIR /app
COPY --from=build /src/target/backend-*.jar app.jar
USER zbob
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
