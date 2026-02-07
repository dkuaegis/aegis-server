FROM gradle:9-jdk25-ubi-minimal AS builder

WORKDIR /tmp

COPY settings.gradle .
COPY build.gradle .

RUN --mount=type=cache,target=/home/gradle/.gradle/caches,id=gradle-cache,sharing=locked \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper,id=gradle-wrapper,sharing=locked \
    gradle --no-daemon dependencies

COPY . .

RUN --mount=type=cache,target=/home/gradle/.gradle/caches,id=gradle-cache,sharing=locked \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper,id=gradle-wrapper,sharing=locked \
    gradle build --no-daemon -x check -x test -x spotlessApply -x spotlessCheck

FROM eclipse-temurin:25-jre-ubi10-minimal

WORKDIR /app

RUN wget -O /app/grafana-opentelemetry-java.jar https://github.com/grafana/grafana-opentelemetry-java/releases/download/v2.22.0/grafana-opentelemetry-java.jar

COPY --from=builder /tmp/build/version.txt /app/version.txt
COPY --from=builder /tmp/build/libs/*-*.jar /app/app.jar

EXPOSE 8080

ENV OTEL_EXPORTER_OTLP_PROTOCOL=grpc
ENV OTEL_SERVICE_NAME=aegis-server
ENV OTEL_SERVICE_NAMESPACE=aegis-web
ENV OTEL_DEPLOYMENT_ENVIRONMENT=prod

ENTRYPOINT ["sh", "-c", "export OTEL_RESOURCE_ATTRIBUTES=\"deployment.environment=${OTEL_DEPLOYMENT_ENVIRONMENT},service.namespace=${OTEL_SERVICE_NAMESPACE},service.version=$(cat /app/version.txt)\" && exec java -javaagent:/app/grafana-opentelemetry-java.jar -jar /app/app.jar 2>&1"]
