# syntax=docker/dockerfile:1.7

FROM bellsoft/liberica-openjdk-alpine:17 AS build
WORKDIR /app
COPY . .
RUN --mount=type=secret,id=github_token \
    export GITHUB_TOKEN="$(cat /run/secrets/github_token)" && \
    ./gradlew clean bootJar --no-daemon

FROM alpine:3.22 AS menu-normalizer-model
ARG MENU_NORMALIZER_MODEL_URL=https://mlrepo.djl.ai/model/nlp/text_embedding/ai/djl/huggingface/pytorch/intfloat/multilingual-e5-small/0.0.1/multilingual-e5-small.zip
ARG MENU_NORMALIZER_MODEL_SHA256=b39a822571db9cd746708e0a87f3209e086b446b080837c1ff142200bf069ced
RUN apk add --no-cache curl unzip
WORKDIR /model
RUN curl --fail --location --retry 3 --output model.zip "$MENU_NORMALIZER_MODEL_URL" && \
    echo "$MENU_NORMALIZER_MODEL_SHA256  model.zip" | sha256sum -c - && \
    unzip -q model.zip && \
    rm model.zip

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/api/build/libs/*.jar app.jar
COPY --from=menu-normalizer-model /model /app/models/multilingual-e5-small

ARG PROFILE
ENV SPRING_PROFILES_ACTIVE=${PROFILE:-dev}
ENV SIKSHA_MENU_NORMALIZER_MODEL_PATH=/app/models/multilingual-e5-small
ENV DJL_OFFLINE=true
ENV OPT_OUT_TRACKING=true
ENV JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=60.0 -XX:MaxRAMPercentage=60.0 -XX:+UseSerialGC -Xss256k"

EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "app.jar"]
