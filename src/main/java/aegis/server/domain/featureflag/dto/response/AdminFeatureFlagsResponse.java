package aegis.server.domain.featureflag.dto.response;

public record AdminFeatureFlagsResponse(
        StudyEnrollWindowFlagResponse studyEnrollWindow, MemberSignupWriteFlagResponse memberSignupWrite) {

    public static AdminFeatureFlagsResponse of(
            StudyEnrollWindowFlagResponse studyEnrollWindow, MemberSignupWriteFlagResponse memberSignupWrite) {
        return new AdminFeatureFlagsResponse(studyEnrollWindow, memberSignupWrite);
    }
}
