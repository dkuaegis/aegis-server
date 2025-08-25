package aegis.server.domain.study.dto.request;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import aegis.server.domain.study.domain.StudyCategory;
import aegis.server.domain.study.domain.StudyLevel;
import aegis.server.domain.study.domain.StudyRecruitmentMethod;

public record StudyCreateUpdateRequest(
        @Size(max = 30) String title,
        StudyCategory category,
        StudyLevel level,
        @Size(max = 1000) String description,
        StudyRecruitmentMethod recruitmentMethod,
        @Min(0) @Max(100) int maxParticipants,
        @Size(max = 100) String schedule,
        @NotNull List<@NotBlank String> curricula,
        @NotNull List<@NotBlank String> qualifications) {}
