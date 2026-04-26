# syntax=docker/dockerfile:1.7

FROM bellsoft/liberica-openjdk-alpine:17 AS build
WORKDIR /app
COPY . .
RUN --mount=type=secret,id=github_token \
    export GITHUB_TOKEN="$(cat /run/secrets/github_token)" && \
    ./gradlew clean bootJar --no-daemon

FROM bellsoft/liberica-openjdk-alpine:17
WORKDIR /app
COPY --from=build /app/api/build/libs/*.jar app.jar

ARG PROFILE
ENV SPRING_PROFILES_ACTIVE=${PROFILE:-dev}
ENV JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=60.0 -XX:MaxRAMPercentage=60.0 -XX:+UseSerialGC -Xss256k"

EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "app.jar"]
