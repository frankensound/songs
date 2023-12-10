FROM eclipse-temurin:21-jdk as build-stage

WORKDIR /build

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x ./gradlew

COPY gradle.properties .

COPY src src

RUN ./gradlew build

FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build-stage /build/build/libs/*.jar /app/songs.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/songs.jar"]