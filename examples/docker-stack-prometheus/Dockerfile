FROM eclipse-temurin:21.0.2_13-jdk
ARG JAR_FILE="target/*.jar"
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
