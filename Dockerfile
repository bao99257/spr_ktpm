# Dockerfile ch�nh ? thu m?c g?c
FROM openjdk:17-jdk-slim
WORKDIR /app
# Sao ch�p c�c file JAR t? c�c module
COPY Library/target/*.jar library.jar
COPY Admin/target/*.jar admin.jar
COPY Customer/target/*.jar customer.jar
# M?c d?nh ch?y service Library
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "library.jar"]
