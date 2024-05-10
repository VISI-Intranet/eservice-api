FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/scala-2.13/srsEservice-assembly-0.1.0-SNAPSHOT.jar ./

CMD ["java", "-jar", "srsEservice-assembly-0.1.0-SNAPSHOT.jar"]
