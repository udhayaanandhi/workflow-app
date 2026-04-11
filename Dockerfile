
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
# Run the app, then start a process that never ends
ENTRYPOINT ["sh", "-c", "java -jar app.jar && tail -f /dev/null"]
