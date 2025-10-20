FROM gradle:8-jdk21-ubi-minimal AS builder

WORKDIR /tmp

COPY settings.gradle .
COPY build.gradle .

RUN gradle --no-daemon dependencies

COPY . .

RUN gradle build --no-daemon -x check -x test -x spotlessApply -x spotlessCheck

FROM eclipse-temurin:21-jre-ubi10-minimal

WORKDIR /app

RUN wget -O /app/grafana-opentelemetry-java.jar https://github.com/grafana/grafana-opentelemetry-java/releases/download/v2.20.1/grafana-opentelemetry-java.jar

COPY --from=builder /tmp/build/version.txt /app/version.txt
COPY --from=builder /tmp/build/libs/*-*.jar /app/app.jar

EXPOSE 8080

ENV OTEL_EXPORTER_OTLP_PROTOCOL=grpc
ENV OTEL_SERVICE_NAME=aegis-server
ENV OTEL_SERVICE_NAMESPACE=aegis-web
ENV OTEL_DEPLOYMENT_ENVIRONMENT=prod

ENTRYPOINT ["sh", "-c", "export OTEL_RESOURCE_ATTRIBUTES=\"deployment.environment=${OTEL_DEPLOYMENT_ENVIRONMENT},service.namespace=${OTEL_SERVICE_NAMESPACE},service.version=$(cat /app/version.txt)\" && exec java -javaagent:/app/grafana-opentelemetry-java.jar -jar /app/app.jar"]
