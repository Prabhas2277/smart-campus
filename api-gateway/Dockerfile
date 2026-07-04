FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY shared-lib ./shared-lib
COPY api-gateway ./api-gateway
RUN mvn -pl shared-lib install -DskipTests
RUN mvn -pl api-gateway package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/api-gateway/target/api-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
