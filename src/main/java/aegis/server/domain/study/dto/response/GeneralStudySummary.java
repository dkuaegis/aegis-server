package aegis.server.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import aegis.server.domain.study.domain.StudyCategory;
import aegis.server.domain.study.domain.StudyLevel;

public record GeneralStudySummary(
        Long id,
        String title,
        StudyCategory category,
        StudyLevel level,
        long participantCount,
        int maxParticipants,
        @Schema(nullable = true) String schedule,
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
