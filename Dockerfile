FROM maven:3.5.2-jdk-8 AS builder
COPY . /usr/src/app/
RUN mvn -f /usr/src/app/mico-core/pom.xml clean package
RUN ls /usr/src/app/mico-core/target

FROM openjdk:8-alpine
VOLUME /tmp
COPY --from=builder /usr/src/app/mico-core/target/mico-core*.jar mico.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/mico.jar"]
