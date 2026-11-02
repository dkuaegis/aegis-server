FROM gradle:9.8.0-jdk25-ubi10@sha256:ac90443ecb45b276de8b736418bd2b2cb6b32de9c36ba8b9bc680aada071479e AS builder

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

FROM eclipse-temurin:25.0.4_7-jre-ubi10-minimal@sha256:9a30637dc9114d1d0abfd6ae1d506e35df1c870fd46b15adfaebca4c0312ec3f AS runtime

WORKDIR /app

RUN wget -O /app/grafana-opentelemetry-java.jar https://github.com/grafana/grafana-opentelemetry-java/releases/download/v2.31.1/grafana-opentelemetry-java.jar

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
