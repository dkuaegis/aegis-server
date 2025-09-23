package aegis.server.domain.study.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudyEnrollRequest(@NotBlank @Size(max = 500) String applicationReason) {}
