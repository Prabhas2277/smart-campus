FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY shared-lib ./shared-lib
COPY auth-service ./auth-service
RUN mvn -pl shared-lib install -DskipTests
RUN mvn -pl auth-service package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/auth-service/target/auth-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
