FROM gradle:8-jdk21-alpine AS builder

WORKDIR /tmp

COPY settings.gradle .
COPY build.gradle .

RUN gradle --no-daemon dependencies

COPY . .

RUN gradle clean build --no-daemon -x check -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Download Grafana OpenTelemetry Java agent
RUN wget -O /app/grafana-opentelemetry-java.jar https://github.com/grafana/grafana-opentelemetry-java/releases/latest/download/grafana-opentelemetry-java.jar

# Install Atlas CLI
RUN arch=$(apk --print-arch) && \
    if [ "$arch" = "x86_64" ]; then ATLAS_ARCH=amd64; \
    elif [ "$arch" = "aarch64" ]; then ATLAS_ARCH=arm64; \
    else echo "unsupported architecture: $arch" && exit 1; fi && \
    wget -q -O /usr/local/bin/atlas https://release.ariga.io/atlas/atlas-linux-${ATLAS_ARCH}-latest && \
    chmod +x /usr/local/bin/atlas

COPY --from=builder /tmp/build/version.txt /app/version.txt
COPY --from=builder /tmp/build/libs/*-*.jar /app/app.jar
COPY migrations /app/migrations
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

EXPOSE 8080

ENV OTEL_EXPORTER_OTLP_PROTOCOL=grpc
ENV OTEL_SERVICE_NAME=aegis-server
ENV OTEL_SERVICE_NAMESPACE=aegis-web
ENV OTEL_DEPLOYMENT_ENVIRONMENT=prod

ENTRYPOINT ["/app/entrypoint.sh"]
