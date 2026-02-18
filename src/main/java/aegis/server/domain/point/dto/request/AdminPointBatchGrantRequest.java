package aegis.server.domain.point.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AdminPointBatchGrantRequest(
        @NotBlank @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$") String requestId,

        @NotNull @Size(min = 1, max = 100) List<@NotNull @Positive Long> memberIds,
        @NotNull @Positive Long amount,
        @NotBlank @Size(max = 200) String reason) {}
