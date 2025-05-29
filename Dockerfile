FROM maven:3-openjdk-17 as build
ARG MAVEN_OPTS="-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=WARN -Dorg.slf4j.simpleLogger.showDateTime=true -Djava.awt.headless=true"

COPY pom.xml /usr/src
WORKDIR /usr/src
COPY . /usr/src
RUN mvn -B package -Dmaven.test.skip=true

FROM alpine:3.17.3 as deploy

RUN apk update && apk add --no-cache --upgrade openjdk17-jre
RUN apk upgrade --available
RUN apk add curl less

RUN adduser app --home /usr/src --shell /bin/sh --disabled-password  && chown -R app.app /usr/src
USER app

WORKDIR /usr/src/app

COPY --from=build /usr/src/target/fdx-mock-auth-server-*.jar app.jar
COPY src/main/resources /usr/src/app/src/main/resources

CMD ["java", "--add-opens", "java.base/java.io=ALL-UNNAMED", "-jar", "app.jar"]
EXPOSE 8080 8081

