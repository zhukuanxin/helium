
FROM maven:3.6.3-openjdk-11 AS build

WORKDIR /app

COPY ./ ./

RUN mkdir /root/.m2/

COPY settings.xml /root/.m2/

RUN mvn package \
    && ls ./target

FROM openjdk:11.0.9.1-jre

WORKDIR /app

COPY --from=build /app/target/dbcheck-proxy-0.0.1-SNAPSHOT.jar ./

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "dbcheck-proxy-0.0.1-SNAPSHOT.jar" ]

