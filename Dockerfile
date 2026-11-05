# Runtime image only - the jar is built by Maven in the CI pipeline
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Run as a non-root user
RUN addgroup -S app && adduser -S app -G app

COPY target/robo-advisor.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
