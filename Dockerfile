FROM docker.io/openjdk:8-jre-slim
COPY ./nucleus/target/nucleus-0.1.0.BUILD-SNAPSHOT.jar /app/gluon/gluon.jar
ADD entrypoint.sh /entrypoint.sh
WORKDIR /app/gluon
EXPOSE 8080


ENTRYPOINT ["/entrypoint.sh"]