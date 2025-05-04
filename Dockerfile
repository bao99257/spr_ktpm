# Dockerfile chính ở thư mục gốc - Multi-stage build
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .

# Cài đặt parent project trước
RUN mvn clean install -N

# Cài đặt từng module riêng biệt
# Đầu tiên là Library vì các module khác phụ thuộc vào nó
RUN cd Library && mvn clean install -Dmaven.test.skip=true

# Sau đó là Admin
RUN cd Admin && mvn clean install -Dmaven.test.skip=true

# Cuối cùng là Customer
RUN cd Customer && mvn clean install -Dmaven.test.skip=true

# Run Stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/Library/target/*.jar library.jar
COPY --from=build /app/Admin/target/*.jar admin.jar
COPY --from=build /app/Customer/target/*.jar customer.jar

# Mặc định chạy service Library
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "library.jar"]












