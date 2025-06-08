FROM eclipse-temurin:17-jre-alpine
WORKDIR /usr/app
COPY app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
