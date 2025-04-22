# Build Stage
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run Stage
FROM openjdk:17-jdk
WORKDIR /app
COPY --from=build /app/Customer/target/*.jar app.jar
EXPOSE 8020
CMD ["java", "-jar", "app.jar"]
