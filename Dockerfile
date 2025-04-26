# gradle build
FROM --platform=linux/x86_64 openjdk:17-jdk-alpine AS build

WORKDIR /app
ARG CODEARTIFACT_AUTH_TOKEN

COPY . .
RUN ./gradlew clean bootJar --no-daemon -PcodeArtifactAuthToken=$CODEARTIFACT_AUTH_TOKEN

# launch jar
FROM --platform=linux/x86_64 openjdk:17-jdk-alpine

WORKDIR /app
COPY --from=build /app/api/build/libs/*.jar app.jar

ARG PROFILE
ENV SPRING_PROFILES_ACTIVE=${PROFILE:-dev}

ENV JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=80.0 -XX:MaxRAMPercentage=80.0"

EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "app.jar"]
