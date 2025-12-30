package aegis.server.domain.activity.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ActivityCreateUpdateRequest(
        @NotEmpty String name, @NotNull @Positive BigDecimal pointAmount) {}
