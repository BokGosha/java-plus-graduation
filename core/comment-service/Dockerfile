FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

# Кэш ~/.m2 общий для всех образов: зависимости скачиваются один раз
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -q -pl core/comment-service -am package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/core/comment-service/target/*.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
