FROM maven:3-amazoncorretto-24 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:24-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/cloud-storage-0.0.1-SNAPSHOT.jar /app/cloud-storage-0.0.1-SNAPSHOT.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/cloud-storage-0.0.1-SNAPSHOT.jar"]
