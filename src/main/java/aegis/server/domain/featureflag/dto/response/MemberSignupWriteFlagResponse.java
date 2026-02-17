package aegis.server.domain.featureflag.dto.response;

public record MemberSignupWriteFlagResponse(
        Long featureFlagId, String rawValue, Boolean enabled, boolean valid, boolean signupWriteAllowed) {

    public static MemberSignupWriteFlagResponse of(
            Long featureFlagId, String rawValue, Boolean enabled, boolean valid, boolean signupWriteAllowed) {
        return new MemberSignupWriteFlagResponse(featureFlagId, rawValue, enabled, valid, signupWriteAllowed);
    }
}
