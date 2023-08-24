FROM openjdk:11-jre-slim
USER root
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} backendservice.jar
ENTRYPOINT ["java","-jar","/backendservice.jar"]