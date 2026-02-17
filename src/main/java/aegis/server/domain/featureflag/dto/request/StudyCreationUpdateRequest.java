package aegis.server.domain.featureflag.dto.request;

import jakarta.validation.constraints.NotNull;

public record StudyCreationUpdateRequest(@NotNull Boolean enabled) {}
