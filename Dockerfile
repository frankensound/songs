# Build stage
FROM eclipse-temurin:21-jdk as build-stage

WORKDIR /build

# Copy Gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build -x test

# Final stage
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the built artifact from the build stage
COPY --from=build-stage /build/build/libs/*.jar /app/songs.jar

# Run the application
CMD ["java", "-jar", "/app/songs.jar"]