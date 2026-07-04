FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY shared-lib ./shared-lib
COPY fee-service ./fee-service
RUN mvn -pl shared-lib install -DskipTests
RUN mvn -pl fee-service package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/fee-service/target/fee-service-*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
