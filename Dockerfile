FROM eclipse-temurin:21-jdk
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} software-engineering.jar
COPY ./src/resources/.env .env
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "software-engineering.jar"]