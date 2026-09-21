FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN mvn -q -pl infra/config-server -am package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/infra/config-server/target/*.jar app.jar
COPY --from=build /app/infra/config-repo /app/infra/config-repo

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
