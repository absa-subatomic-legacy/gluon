FROM docker.io/openjdk:8-jre-slim
COPY ./nucleus/target/nucleus-0.1.0.BUILD-SNAPSHOT.jar /app/gluon/gluon.jar
WORKDIR /app/gluon
EXPOSE 8080
CMD ["java", "$JAVA_OPTS", "-jar", "gluon.jar"]