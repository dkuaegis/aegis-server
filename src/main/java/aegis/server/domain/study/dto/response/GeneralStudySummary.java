package aegis.server.domain.study.dto.response;

import aegis.server.domain.study.domain.StudyCategory;
import aegis.server.domain.study.domain.StudyLevel;

public record GeneralStudySummary(
        Long id,
        String title,
        StudyCategory category,
        StudyLevel level,
        long participantCount,
        int maxParticipants,
        String schedule,
        String instructor) {

    public static GeneralStudySummary from(
            Long id,
            String title,
            StudyCategory category,
            StudyLevel level,
            long participantCount,
            int maxParticipants,
            String schedule,
            String instructor) {
        return new GeneralStudySummary(
                id, title, category, level, participantCount, maxParticipants, schedule, instructor);
    }
}
