FROM eclipse-temurin:21-jdk
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} software-engineering.jar
RUN --mount=type=secret,id=env_content,target=.env \
  cp /run/secrets/env_content .env
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "software-engineering.jar"]