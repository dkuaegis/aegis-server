package aegis.server.domain.featureflag.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record StudyEnrollWindowUpdateRequest(
        @NotNull LocalDateTime openAt, @NotNull LocalDateTime closeAt) {}
