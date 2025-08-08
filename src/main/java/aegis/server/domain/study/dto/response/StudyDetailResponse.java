package aegis.server.domain.study.dto.response;

import aegis.server.domain.study.domain.StudyCategory;
import aegis.server.domain.study.domain.StudyLevel;
import aegis.server.domain.study.domain.StudyRecruitmentMethod;

public record StudyDetailResponse(
        Long id,
        String title,
        StudyCategory category,
        StudyLevel level,
        String description,
        StudyRecruitmentMethod recruitmentMethod,
        long participantCount,
        int maxParticipants,
        String schedule,
        String curricula,
        String qualifications,
        String instructor) {

    public static StudyDetailResponse from(
            Long id,
            String title,
            StudyCategory category,
            StudyLevel level,
            String description,
            StudyRecruitmentMethod recruitmentMethod,
            long participantCount,
            int maxParticipants,
            String schedule,
            String curricula,
            String qualifications,
            String instructor) {
        return new StudyDetailResponse(
                id,
                title,
                category,
                level,
                description,
                recruitmentMethod,
                participantCount,
                maxParticipants,
                schedule,
                curricula,
                qualifications,
                instructor);
    }
}
