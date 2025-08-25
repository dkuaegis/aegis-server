package aegis.server.domain.common.converter;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StringListJsonConverterTest {

    private final StringListJsonConverter converter = new StringListJsonConverter();

    @Nested
    @DisplayName("convertToDatabaseColumn")
    class ToDatabase {
        @Test
        @DisplayName("null 이면 null 반환")
        void nullInput() {
            assertThat(converter.convertToDatabaseColumn(null)).isNull();
        }

        @Test
        @DisplayName("빈 리스트는 '[]' 로 직렬화")
        void emptyList() {
            assertThat(converter.convertToDatabaseColumn(List.of())).isEqualTo("[]");
        }

        @Test
        @DisplayName("일반 문자열 리스트를 JSON 배열 문자열로 직렬화")
        void normalList() {
            String json = converter.convertToDatabaseColumn(List.of("a", "b", "c"));
            assertThat(json).isEqualTo("[\"a\",\"b\",\"c\"]");
        }

        @Test
        @DisplayName("특수문자를 포함해도 올바르게 이스케이프")
        void specialChars() {
            String json = converter.convertToDatabaseColumn(List.of("a,b", "c\"d", " e "));
            assertThat(json).isEqualTo("[\"a,b\",\"c\\\"d\",\" e \"]");
        }
    }

    @Nested
    @DisplayName("convertToEntityAttribute")
    class ToEntity {
        @Test
        @DisplayName("null/blank 는 빈 리스트 반환")
        void nullOrBlank() {
            assertThat(converter.convertToEntityAttribute(null)).isEmpty();
            assertThat(converter.convertToEntityAttribute("")).isEmpty();
            assertThat(converter.convertToEntityAttribute("   ")).isEmpty();
        }

        @Test
        @DisplayName("JSON 배열 문자열을 리스트로 역직렬화")
        void jsonArray() {
            List<String> list = converter.convertToEntityAttribute("[\"a\",\"b\",\"c\"]");
            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("잘못된 JSON 은 예외 발생")
        void malformedJson() {
            assertThatThrownBy(() -> converter.convertToEntityAttribute("not-json"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
