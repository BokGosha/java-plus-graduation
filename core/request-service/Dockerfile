FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -q -pl core/request-service -am package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/core/request-service/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
