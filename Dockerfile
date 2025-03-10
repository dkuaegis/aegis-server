FROM gradle:8-jdk21 AS builder

WORKDIR /tmp

COPY settings.gradle .
COPY build.gradle .

RUN gradle --no-daemon dependencies

COPY . .

RUN gradle clean build --no-daemon -x check -x test

RUN  wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /tmp/build/libs/*-SNAPSHOT.jar /app/app.jar

COPY --from=builder /tmp/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

EXPOSE 8080

ENV OTEL_EXPORTER_OTLP_ENDPOINT=""
ENV OTEL_SERVICE_NAME="aegis-server"

ENTRYPOINT ["java", \
    "-javaagent:/app/opentelemetry-javaagent.jar", \
    "-jar", "/app/app.jar"]
