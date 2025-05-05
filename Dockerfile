# Dockerfile chính ở thư mục gốc - Multi-stage build
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .

# Hiển thị cấu trúc thư mục để kiểm tra
RUN ls -la

# Cài đặt parent project trước
RUN mvn clean install -N

# Cài đặt Library module trước với debug
RUN mvn clean install -pl Library -DskipTests -X

# Kiểm tra xem Library đã được cài đặt vào local repository chưa
RUN ls -la /root/.m2/repository/bd/edu/diu/cis/

# Kiểm tra nội dung JAR file của Library
RUN find /root/.m2/repository/bd/edu/diu/cis/ -name "*.jar" -exec jar tvf {} \;

# Cài đặt Admin và Customer với debug
RUN mvn clean package -pl Admin,Customer -DskipTests -X

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


















