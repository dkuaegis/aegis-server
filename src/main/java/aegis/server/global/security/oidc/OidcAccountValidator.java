package aegis.server.global.security.oidc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class OidcAccountValidator {

    static final String INVALID_OIDC_ACCOUNT = "INVALID_OIDC_ACCOUNT";
    static final String NOT_DKU_EMAIL = "NOT_DKU_EMAIL";

    private static final String DANKOOK_HOSTED_DOMAIN = "dankook.ac.kr";
    private static final String HOSTED_DOMAIN_CLAIM = "hd";

    @Value("${email-restriction.enabled}")
    private boolean emailRestrictionEnabled;

    @Value("${email-restriction.admin-email}")
    private String adminEmail;

    public void validate(OidcUser oidcUser) {
        String email = oidcUser.getEmail();

        if (!StringUtils.hasText(oidcUser.getSubject())
                || !StringUtils.hasText(email)
                || !StringUtils.hasText(oidcUser.getFullName())
                || !Boolean.TRUE.equals(oidcUser.getEmailVerified())) {
            throw authenticationException(INVALID_OIDC_ACCOUNT);
        }

        if (emailRestrictionEnabled
                && !email.equalsIgnoreCase(adminEmail)
                && !DANKOOK_HOSTED_DOMAIN.equalsIgnoreCase(oidcUser.getClaimAsString(HOSTED_DOMAIN_CLAIM))) {
            throw authenticationException(NOT_DKU_EMAIL);
        }
    }

    private OAuth2AuthenticationException authenticationException(String errorCode) {
        return new OAuth2AuthenticationException(new OAuth2Error(errorCode));
    }
}
