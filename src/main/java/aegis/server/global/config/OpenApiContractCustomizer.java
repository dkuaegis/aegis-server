package aegis.server.global.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;

import org.springframework.stereotype.Component;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.PropertyCustomizer;

import aegis.server.global.exception.ErrorCode;

/**
 * 클라이언트 코드 생성기가 그대로 사용할 수 있도록 OpenAPI 문서를 보강합니다.
 *
 * <ul>
 *   <li>모든 오류 응답에 {@code ErrorResponse} 본문을 연결합니다. 컨트롤러는 오류 응답을 빈 {@code @Content}로 적어도 됩니다.
 *   <li>응답 DTO의 모든 속성을 required로 표시합니다. null이 될 수 있는 속성은 {@code @Schema(nullable = true)}로 표시합니다.
 *   <li>OpenAPI 3.0은 {@code $ref} 옆의 nullable을 무시하므로, nullable인 참조 속성은 {@code allOf}로 감싸서 nullable을 유지합니다.
 * </ul>
 */
@Component
public class OpenApiContractCustomizer implements OpenApiCustomizer, PropertyCustomizer {

    private static final String ERROR_CODE_SCHEMA = "ErrorCode";
    private static final String ERROR_RESPONSE_SCHEMA = "ErrorResponse";
    private static final String SCHEMA_REF_PREFIX = "#/components/schemas/";
    private static final String JSON_MEDIA_TYPE = "application/json";

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(new Components());
        }
        registerErrorSchemas(openApi.getComponents());
        markResponsePropertiesRequired(openApi.getComponents());
        attachErrorResponseBodies(openApi);
    }

    @Override
    public Schema customize(Schema property, AnnotatedType type) {
        if (property == null || property.get$ref() == null || !isMarkedNullable(type)) {
            return property;
        }
        ComposedSchema nullableReference = new ComposedSchema();
        nullableReference.addAllOfItem(new Schema<>().$ref(property.get$ref()));
        nullableReference.setNullable(true);
        return nullableReference;
    }

    private void registerErrorSchemas(Components components) {
        StringSchema errorCodeSchema = new StringSchema();
        errorCodeSchema.setEnum(
                Arrays.stream(ErrorCode.values()).map(Enum::name).toList());
        components.addSchemas(ERROR_CODE_SCHEMA, errorCodeSchema);

        ObjectSchema errorResponseSchema = new ObjectSchema();
        errorResponseSchema.addProperty("name", new Schema<>().$ref(SCHEMA_REF_PREFIX + ERROR_CODE_SCHEMA));
        errorResponseSchema.addRequiredItem("name");
        components.addSchemas(ERROR_RESPONSE_SCHEMA, errorResponseSchema);
    }

    private void markResponsePropertiesRequired(Components components) {
        Map<String, Schema> schemas = components.getSchemas();
        if (schemas == null) {
            return;
        }
        schemas.forEach((name, schema) -> {
            // swagger-core가 nullable 속성의 어노테이션을 처음 만난 모델 자체에 옮겨 적는 경우가 있어, 모델 수준 nullable은 제거합니다.
            schema.setNullable(null);
            if (name.endsWith("Request") || schema.getProperties() == null) {
                return;
            }
            List<String> required = new ArrayList<>(schema.getProperties().keySet());
            schema.setRequired(required);
        });
    }

    private void attachErrorResponseBodies(OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            return;
        }
        for (PathItem pathItem : openApi.getPaths().values()) {
            for (Operation operation : pathItem.readOperations()) {
                if (operation.getResponses() == null) {
                    continue;
                }
                operation.getResponses().forEach(this::attachErrorResponseBody);
            }
        }
    }

    private void attachErrorResponseBody(String statusCode, ApiResponse response) {
        boolean isError = statusCode.startsWith("4") || statusCode.startsWith("5");
        boolean hasBody =
                response.getContent() != null && !response.getContent().isEmpty();
        if (!isError || hasBody) {
            return;
        }
        response.setContent(new Content()
                .addMediaType(
                        JSON_MEDIA_TYPE,
                        new MediaType().schema(new Schema<>().$ref(SCHEMA_REF_PREFIX + ERROR_RESPONSE_SCHEMA))));
    }

    private boolean isMarkedNullable(AnnotatedType type) {
        if (type.getCtxAnnotations() == null) {
            return false;
        }
        return Arrays.stream(type.getCtxAnnotations())
                .anyMatch(annotation ->
                        annotation instanceof io.swagger.v3.oas.annotations.media.Schema schema && schema.nullable());
    }
}
