FROM openjdk:17-jdk-slim

WORKDIR /app

COPY . .
RUN mvn clean package -DskipTests

EXPOSE 8888

CMD ["java", "-jar", "target/config-1.0.jar"]