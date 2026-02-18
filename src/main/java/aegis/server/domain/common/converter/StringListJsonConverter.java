package aegis.server.domain.common.converter;

import java.util.Collections;
import java.util.List;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Converter
public class StringListJsonConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Failed to serialize List<String> to JSON", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(
                    dbData, OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Failed to deserialize JSON to List<String>", e);
        }
    }
}
