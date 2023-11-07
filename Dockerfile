# Use the official openjdk image to run the application
FROM openjdk:17-jdk-slim
ADD target/*.jar msreconciliation-0.0.1-SNAPSHOT.jar
EXPOSE 8088
ENTRYPOINT ["java","-jar","msreconciliation-0.0.1-SNAPSHOT.jar"]