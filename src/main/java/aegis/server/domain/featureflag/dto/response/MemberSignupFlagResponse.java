package aegis.server.domain.featureflag.dto.response;

public record MemberSignupFlagResponse(
        Long featureFlagId, String rawValue, Boolean enabled, boolean valid, boolean signupAllowed) {

    public static MemberSignupFlagResponse of(
            Long featureFlagId, String rawValue, Boolean enabled, boolean valid, boolean signupAllowed) {
        return new MemberSignupFlagResponse(featureFlagId, rawValue, enabled, valid, signupAllowed);
    }
}
