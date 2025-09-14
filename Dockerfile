# syntax=docker/dockerfile:1.7

FROM bellsoft/liberica-openjdk-alpine:17 AS build
WORKDIR /app
COPY . .
RUN --mount=type=secret,id=codeartifact \
    TOKEN="$(cat /run/secrets/codeartifact)" && \
    ./gradlew clean bootJar --no-daemon -PcodeArtifactAuthToken="$TOKEN"

FROM bellsoft/liberica-openjdk-alpine:17-jre
WORKDIR /app
COPY --from=build /app/api/build/libs/*.jar app.jar

ARG PROFILE
ENV SPRING_PROFILES_ACTIVE=${PROFILE:-dev}
ENV JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=60.0 -XX:MaxRAMPercentage=60.0 -XX:+UseSerialGC -Xss256k"

EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "app.jar"]
