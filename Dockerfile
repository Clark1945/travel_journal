FROM eclipse-temurin:17-jre-alpine
EXPOSE 8080
COPY app.jar /usr/app/app.jar
WORKDIR /usr/app
ENTRYPOINT ["java", "-jar", "app.jar"]
