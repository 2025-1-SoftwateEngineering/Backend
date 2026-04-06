FROM eclipse-temurin:21-jdk
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} software-engineering.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "software-engineering.jar"]