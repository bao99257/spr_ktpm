# Dockerfile chính ở thư mục gốc - Multi-stage build
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
# Cài đặt Library trước
RUN mvn clean install -pl Library -am -DskipTests
# Sau đó build tất cả
RUN mvn clean package -DskipTests

# Run Stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/Library/target/*.jar library.jar
COPY --from=build /app/Admin/target/*.jar admin.jar
COPY --from=build /app/Customer/target/*.jar customer.jar

# Mặc định chạy service Library
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "library.jar"]







