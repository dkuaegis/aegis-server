package aegis.server.domain.featureflag.dto.response;

import java.time.LocalDateTime;

public record StudyEnrollWindowFlagResponse(
        Long openFlagId,
        Long closeFlagId,
        String openAtRaw,
        String closeAtRaw,
        LocalDateTime openAt,
        LocalDateTime closeAt,
        boolean valid,
        boolean enrollmentAllowedNow) {

    public static StudyEnrollWindowFlagResponse of(
            Long openFlagId,
            Long closeFlagId,
            String openAtRaw,
            String closeAtRaw,
            LocalDateTime openAt,
            LocalDateTime closeAt,
            boolean valid,
            boolean enrollmentAllowedNow) {
        return new StudyEnrollWindowFlagResponse(
                openFlagId, closeFlagId, openAtRaw, closeAtRaw, openAt, closeAt, valid, enrollmentAllowedNow);
    }
}
