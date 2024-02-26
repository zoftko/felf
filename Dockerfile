FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

COPY / /src

RUN apk add --update npm;
RUN mvn -Dmaven.test.skip -f /src/felf/pom.xml clean package

FROM eclipse-temurin:21-jre-alpine
COPY --from=build /src/felf/target/felf.jar /opt/app/felf.jar
EXPOSE 8080
CMD ["java", "-jar", "/opt/app/felf.jar"]
