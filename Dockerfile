FROM maven:3.5.2-jdk-8 AS build
COPY . /usr/src/app/
RUN mvn -f /usr/src/app/mico-core/pom.xml clean package