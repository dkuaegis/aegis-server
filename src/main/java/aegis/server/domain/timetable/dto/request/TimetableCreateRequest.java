package aegis.server.domain.timetable.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TimetableCreateRequest(
        @NotBlank String url
) {
}
