FROM eclipse-temurin:17-jdk

ARG JAR_FILE=target/lab1.jar
COPY ${JAR_FILE} lab1.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/lab1.jar"]