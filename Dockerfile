FROM gradle:9.8.2-jdk25-ubi10@sha256:d14c6786cc6958f61a7f270714f16ea4e0dabcba0a0d19ebcfe8d30e3c878cd4 AS builder

WORKDIR /tmp

COPY settings.gradle .
COPY build.gradle .
COPY gradle.properties .

RUN --mount=type=cache,target=/home/gradle/.gradle/caches,id=gradle-cache,sharing=locked \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper,id=gradle-wrapper,sharing=locked \
    gradle --no-daemon dependencies

COPY . .

RUN --mount=type=cache,target=/home/gradle/.gradle/caches,id=gradle-cache,sharing=locked \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper,id=gradle-wrapper,sharing=locked \
    gradle build --no-daemon -x check -x test -x spotlessApply -x spotlessCheck

FROM eclipse-temurin:25.0.5_8-jre-ubi10-minimal@sha256:48e4caedcee0aa7aea413ca57bea932415e17f4eaddeef35c3a41085d59d7f85 AS runtime

WORKDIR /app

RUN wget -O /app/grafana-opentelemetry-java.jar https://github.com/grafana/grafana-opentelemetry-java/releases/download/v2.33.1/grafana-opentelemetry-java.jar

EXPOSE 8080

ENV OTEL_EXPORTER_OTLP_PROTOCOL=grpc
ENV OTEL_SERVICE_NAME=aegis-server
ENV OTEL_SERVICE_NAMESPACE=aegis-web
ENV OTEL_DEPLOYMENT_ENVIRONMENT=prod
ENV TZ=Asia/Seoul
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Seoul"

ENTRYPOINT ["sh", "-c", "export OTEL_RESOURCE_ATTRIBUTES=\"deployment.environment=${OTEL_DEPLOYMENT_ENVIRONMENT},service.namespace=${OTEL_SERVICE_NAMESPACE},service.version=$(cat /app/version.txt)\" && exec java -javaagent:/app/grafana-opentelemetry-java.jar -jar /app/app.jar 2>&1"]

COPY --from=builder /tmp/build/version.txt /app/version.txt
COPY --from=builder /tmp/build/libs/*-*.jar /app/app.jar
