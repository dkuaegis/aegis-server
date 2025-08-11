package aegis.server.domain.study.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
        @Size(max = 1000) String curricula,
        @Size(max = 1000) String qualifications) {}
