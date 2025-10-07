FROM openjdk:24-jdk-slim
LABEL authors="arthu"
COPY target/

ENTRYPOINT ["top", "-b"]