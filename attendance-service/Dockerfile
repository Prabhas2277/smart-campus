FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY shared-lib ./shared-lib
COPY attendance-service ./attendance-service
RUN mvn -pl shared-lib install -DskipTests
RUN mvn -pl attendance-service package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/attendance-service/target/attendance-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
