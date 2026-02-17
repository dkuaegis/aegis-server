package aegis.server.domain.featureflag.service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.featureflag.domain.FeatureFlagKey;
import aegis.server.domain.featureflag.domain.FeatureFlagValueType;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeaturePolicyService {

    private static final List<DateTimeFormatter> SUPPORTED_DATE_TIME_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

    private final FeatureFlagService featureFlagService;
    private final Clock clock;

    public boolean isStudyEnrollmentAllowed() {
        return evaluateStudyEnrollWindow().enrollmentAllowedNow();
    }

    public StudyEnrollWindowEvaluation evaluateStudyEnrollWindow() {
        FeatureFlagSnapshot openSnapshot = featureFlagService
                .findCachedFlag(FeatureFlagKey.STUDY_ENROLL_WINDOW_OPEN_AT)
                .orElse(null);
        FeatureFlagSnapshot closeSnapshot = featureFlagService
                .findCachedFlag(FeatureFlagKey.STUDY_ENROLL_WINDOW_CLOSE_AT)
                .orElse(null);

        String openRaw = openSnapshot == null ? null : openSnapshot.value();
        String closeRaw = closeSnapshot == null ? null : closeSnapshot.value();
        Long openFlagId = openSnapshot == null ? null : openSnapshot.id();
        Long closeFlagId = closeSnapshot == null ? null : closeSnapshot.id();

        LocalDateTime openAt = parseLocalDateTime(openSnapshot);
        LocalDateTime closeAt = parseLocalDateTime(closeSnapshot);

        boolean valid = openAt != null && closeAt != null && closeAt.isAfter(openAt);
        if (!valid) {
            return StudyEnrollWindowEvaluation.of(
                    openFlagId, closeFlagId, openRaw, closeRaw, openAt, closeAt, false, true);
        }

        ZoneId zoneId = clock.getZone();
        ZonedDateTime now = ZonedDateTime.now(clock);
        boolean allowed = !now.isBefore(openAt.atZone(zoneId)) && now.isBefore(closeAt.atZone(zoneId));

        return StudyEnrollWindowEvaluation.of(
                openFlagId, closeFlagId, openRaw, closeRaw, openAt, closeAt, true, allowed);
    }

    private static LocalDateTime parseLocalDateTime(FeatureFlagSnapshot snapshot) {
        if (snapshot == null
                || snapshot.valueType() != FeatureFlagValueType.LOCAL_DATE_TIME
                || isBlank(snapshot.value())) {
            return null;
        }

        for (DateTimeFormatter formatter : SUPPORTED_DATE_TIME_FORMATS) {
            try {
                return LocalDateTime.parse(snapshot.value(), formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record StudyEnrollWindowEvaluation(
            Long openFlagId,
            Long closeFlagId,
            String openAtRaw,
            String closeAtRaw,
            LocalDateTime openAt,
            LocalDateTime closeAt,
            boolean valid,
            boolean enrollmentAllowedNow) {

        public static StudyEnrollWindowEvaluation of(
                Long openFlagId,
                Long closeFlagId,
                String openAtRaw,
                String closeAtRaw,
                LocalDateTime openAt,
                LocalDateTime closeAt,
                boolean valid,
                boolean enrollmentAllowedNow) {
            return new StudyEnrollWindowEvaluation(
                    openFlagId, closeFlagId, openAtRaw, closeAtRaw, openAt, closeAt, valid, enrollmentAllowedNow);
        }
    }
}
