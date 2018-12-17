FROM maven:3.5.2-jdk-8 AS builder
#Cache dependencys
COPY pom.xml /usr/src/app/
COPY mico-core/pom.xml /usr/src/app/mico-core/pom.xml
RUN mvn -f /usr/src/app/mico-core/pom.xml dependency:go-offline

COPY . /usr/src/app/
RUN mvn -f /usr/src/app/mico-core/pom.xml clean package -Dmaven.test.skip=true

FROM openjdk:8-alpine
VOLUME /tmp
COPY --from=builder /usr/src/app/mico-core/target/mico-core*.jar mico.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/mico.jar"]
