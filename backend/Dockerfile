FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy gradle wrapper + config trước (cache dependency)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x gradlew

# tải dependency
RUN ./gradlew dependencies --no-daemon

EXPOSE 8080

CMD ["./gradlew", "bootRun", "--no-daemon"]