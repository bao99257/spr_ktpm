# Dockerfile chính ở thư mục gốc - Multi-stage build
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .

# Cài đặt parent project trước
RUN mvn clean install -N -Dmaven.test.skip=true

# Cài đặt Library module với debug để xem chi tiết
RUN mvn clean install -pl Library -Dmaven.test.skip=true

# Cài đặt Admin và Customer với debug
RUN mvn clean package -pl Admin,Customer -Dmaven.test.skip=true

# Run Stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy các jar files
COPY --from=build /app/Library/target/*.jar /app/library.jar
COPY --from=build /app/Admin/target/*.jar /app/admin.jar
COPY --from=build /app/Customer/target/*.jar /app/customer.jar

# Mặc định chạy service Library
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/library.jar"]













