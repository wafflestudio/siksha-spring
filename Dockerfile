# gradle build
FROM --platform=linux/x86_64 openjdk:17-jdk-alpine AS build

WORKDIR /app

COPY . .
RUN ./gradlew clean bootJar --no-daemon

# launch jar
FROM --platform=linux/x86_64 openjdk:17-jdk-alpine

WORKDIR /app
COPY --from=build /app/api/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "app.jar"]
