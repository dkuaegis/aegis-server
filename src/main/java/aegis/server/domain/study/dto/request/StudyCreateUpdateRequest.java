package aegis.server.domain.study.dto.request;

import aegis.server.domain.study.domain.StudyCategory;
import aegis.server.domain.study.domain.StudyLevel;
import aegis.server.domain.study.domain.StudyRecruitmentMethod;

public record StudyCreateUpdateRequest(
        String title,
        StudyCategory category,
        StudyLevel level,
        String description,
        StudyRecruitmentMethod recruitmentMethod,
        String maxParticipants,
        String schedule,
        String curricula,
        String qualifications) {}
