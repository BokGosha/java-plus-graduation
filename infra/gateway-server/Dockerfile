FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

# Кэш ~/.m2 общий для всех образов: зависимости скачиваются один раз
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -q -pl infra/gateway-server -am package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/infra/gateway-server/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
