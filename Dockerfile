FROM eclipse-temurin:21-jdk
WORKDIR /app

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} software-engineering.jar
RUN --mount=type=secret,id=env \
  cp /run/secrets/env /app/.env

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "software-engineering.jar"]