package aegis.server.domain.study.dto.response;

import java.util.List;

import aegis.server.domain.study.domain.StudyCategory;
import aegis.server.domain.study.domain.StudyLevel;
import aegis.server.domain.study.domain.StudyRecruitmentMethod;

public record GeneralStudyDetail(
        Long id,
        String title,
        StudyCategory category,
        StudyLevel level,
        String description,
        StudyRecruitmentMethod recruitmentMethod,
        long participantCount,
        int maxParticipants,
        String schedule,
        List<String> curricula,
        List<String> qualifications,
        String instructor) {

    public static GeneralStudyDetail from(
            Long id,
            String title,
            StudyCategory category,
            StudyLevel level,
            String description,
            StudyRecruitmentMethod recruitmentMethod,
            long participantCount,
            int maxParticipants,
            String schedule,
            List<String> curricula,
            List<String> qualifications,
            String instructor) {
        return new GeneralStudyDetail(
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
