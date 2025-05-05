# Dockerfile chính ở thư mục gốc - Multi-stage build
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .

# Tải tất cả các dependency trước
RUN mvn dependency:go-offline

# Cài đặt parent project trước
RUN mvn clean install -N -o

# Cài đặt Library module trước
RUN mvn clean install -pl Library -DskipTests -o

# Cài đặt Admin và Customer
RUN mvn clean package -pl Admin,Customer -DskipTests -o

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




















