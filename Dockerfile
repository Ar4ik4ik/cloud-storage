FROM openjdk:24-jdk-slim
LABEL authors="arthu"
COPY target/.*jar application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]