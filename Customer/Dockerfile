# Build Stage
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean install -N
RUN mvn clean install -pl Library -DskipTests
RUN mvn clean package -pl Customer -DskipTests

# Run Stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/Customer/target/*.jar /app/app.jar
EXPOSE 8020
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
