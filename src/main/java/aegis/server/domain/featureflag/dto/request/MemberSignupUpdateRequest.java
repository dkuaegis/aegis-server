package aegis.server.domain.featureflag.dto.request;

import jakarta.validation.constraints.NotNull;

public record MemberSignupUpdateRequest(@NotNull Boolean enabled) {}
