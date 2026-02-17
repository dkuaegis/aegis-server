package aegis.server.domain.featureflag.dto.request;

import jakarta.validation.constraints.NotNull;

public record MemberSignupWriteUpdateRequest(@NotNull Boolean enabled) {}
