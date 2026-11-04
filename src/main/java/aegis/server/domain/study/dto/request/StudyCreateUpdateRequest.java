package aegis.server.domain.study.dto.request;

import java.util.List;

import jakarta.validation.constraints.*;

import io.swagger.v3.oas.annotations.media.Schema;

import aegis.server.domain.study.domain.StudyCategory;
import aegis.server.domain.study.domain.StudyLevel;
import aegis.server.domain.study.domain.StudyRecruitmentMethod;

public record StudyCreateUpdateRequest(
        @NotBlank @Size(max = 30) String title,
        @NotNull StudyCategory category,
        @NotNull StudyLevel level,
        @NotBlank @Size(max = 1000) String description,
        @NotNull StudyRecruitmentMethod recruitmentMethod,

        @Schema(description = "스터디 정원 (0=무제한), 최대 100") @Min(0) @Max(100) int maxParticipants,

        @Schema(nullable = true) @Size(max = 100) String schedule,
        @NotNull List<@NotBlank String> curricula,
        @NotNull List<@NotBlank String> qualifications) {}
