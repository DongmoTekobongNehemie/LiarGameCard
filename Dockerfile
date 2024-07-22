FROM eclipse-temurin:21.0.3_9-jdk
WORKDIR /APP
COPY target/LiarCardGame-0.0.1-SNAPSHOT.jar LiarCardGame.jar
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "LiarCardGame.jar"]
