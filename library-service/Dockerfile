FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY shared-lib ./shared-lib
COPY library-service ./library-service
RUN mvn -pl shared-lib install -DskipTests
RUN mvn -pl library-service package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/library-service/target/library-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
