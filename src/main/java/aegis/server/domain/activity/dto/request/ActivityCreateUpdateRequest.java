package aegis.server.domain.activity.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record ActivityCreateUpdateRequest(@NotEmpty String name) {}
