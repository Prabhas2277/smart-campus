FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY shared-lib ./shared-lib
COPY hostel-service ./hostel-service
RUN mvn -pl shared-lib install -DskipTests
RUN mvn -pl hostel-service package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/hostel-service/target/hostel-service-*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
