#!/usr/bin/env sh
set -eu

# Required DB config
: "${DB_HOST:?Missing DB_HOST}"
: "${DB_PORT:?Missing DB_PORT}"
: "${DB_NAME:?Missing DB_NAME}"
: "${DB_USER:?Missing DB_USER}"
: "${DB_PASSWORD:?Missing DB_PASSWORD}"

# Build URL (internal network: sslmode=disable, search_path=public)
DB_URL="postgresql://${DB_USER}:${DB_PASSWORD}@${DB_HOST}:${DB_PORT}/${DB_NAME}?sslmode=disable&search_path=public"

# Bseline version
BASELINE_VERSION="20250905061014"

echo "Applying DB migrations (baseline=$BASELINE_VERSION)..."
if ! atlas migrate apply --url "$DB_URL" --dir file:///app/migrations --baseline "$BASELINE_VERSION"; then
  echo "Migration failed. Exiting."
  exit 1
fi
echo "Migrations applied."

# Set OTEL resource attributes
VERSION=$(cat /app/version.txt 2>/dev/null || echo "unknown")
OTEL_DEPLOYMENT_ENVIRONMENT="${OTEL_DEPLOYMENT_ENVIRONMENT:-prod}"
OTEL_SERVICE_NAMESPACE="${OTEL_SERVICE_NAMESPACE:-aegis-web}"
export OTEL_RESOURCE_ATTRIBUTES="deployment.environment=${OTEL_DEPLOYMENT_ENVIRONMENT},service.namespace=${OTEL_SERVICE_NAMESPACE},service.version=${VERSION}"

echo "Starting application..."
exec java -javaagent:/app/grafana-opentelemetry-java.jar -jar /app/app.jar
