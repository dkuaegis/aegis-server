FROM gradle:8-jdk21 AS builder

WORKDIR /tmp

COPY settings.gradle .
COPY build.gradle .

RUN gradle --no-daemon dependencies

COPY . .

RUN gradle clean build --no-daemon -x check -x test

RUN curl -O https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip newrelic-java.zip

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /tmp/build/libs/*-SNAPSHOT.jar /app/app.jar

COPY --from=builder /tmp/newrelic/newrelic.jar /app/newrelic.jar

COPY newrelic.yml /app/newrelic.yml

ENV NEW_RELIC_LICENSE_KEY=""

EXPOSE 8080

ENTRYPOINT ["java", \
    "-javaagent:/app/newrelic.jar", \
    "-Dnewrelic.license_key=${NEW_RELIC_LICENSE_KEY}", \
    "-Dnewrelic.environment=production", \
    "-Dnewrelic.config.file=/app/newrelic.yml", \
    "-jar", "/app/app.jar"]
