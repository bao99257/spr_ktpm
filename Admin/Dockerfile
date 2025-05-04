# Dockerfile chính ở thư mục gốc
FROM openjdk:17-jdk-slim
WORKDIR /app

# Sao chép các file JAR từ các module
COPY Library/target/*.jar library.jar
COPY Admin/target/*.jar admin.jar
COPY Customer/target/*.jar customer.jar

# Mặc định chạy service Library
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "library.jar"]
