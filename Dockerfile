# Dockerfile chính ở thư mục gốc - Multi-stage build
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .

# Hiển thị cấu trúc thư mục để kiểm tra
RUN ls -la && ls -la Library && ls -la Admin

# Kiểm tra pom.xml của các module
RUN cat pom.xml
RUN cat Library/pom.xml
RUN cat Admin/pom.xml

# Cài đặt parent project
RUN mvn clean install -N

# Cài đặt Library module và cài đặt vào local repository
RUN cd Library && mvn clean install -DskipTests

# Cài đặt Admin module sau khi Library đã được cài đặt
RUN cd Admin && mvn clean package -DskipTests

# Cài đặt Customer module
RUN cd Customer && mvn clean package -DskipTests

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














