FROM eclipse-temurin:17-jre-alpine

EXPOSE 8080

# 假設 jar 名稱已確定
COPY ./build/libs/my-app-1.0-SNAPSHOT.jar /usr/app/
WORKDIR /usr/app

ENTRYPOINT ["java", "-jar", "my-app-1.0-SNAPSHOT.jar"]