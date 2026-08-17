package aegis.server.global.security.oidc;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OidcAccountValidatorTest {

    private OidcAccountValidator validator;

    @BeforeEach
    void setUp() {
        validator = new OidcAccountValidator();
        ReflectionTestUtils.setField(validator, "emailRestrictionEnabled", true);
        ReflectionTestUtils.setField(validator, "adminEmail", "dankook.aegis@gmail.com");
    }

    @Test
    void 단국대학교가_관리하는_이메일_계정을_허용한다() {
        OidcUser oidcUser = createOidcUser("member@dankook.ac.kr", true, "dankook.ac.kr");

        assertThatCode(() -> validator.validate(oidcUser)).doesNotThrowAnyException();
    }

    @Test
    void 이메일_주소만_단국대학교_도메인인_계정은_거부한다() {
        OidcUser oidcUser = createOidcUser("member@dankook.ac.kr", true, null);

        assertThatThrownBy(() -> validator.validate(oidcUser))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .extracting(exception ->
                        ((OAuth2AuthenticationException) exception).getError().getErrorCode())
                .isEqualTo(OidcAccountValidator.NOT_DKU_EMAIL);
    }

    @Test
    void 검증되지_않은_이메일_계정은_거부한다() {
        OidcUser oidcUser = createOidcUser("member@dankook.ac.kr", false, "dankook.ac.kr");

        assertThatThrownBy(() -> validator.validate(oidcUser))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .extracting(exception ->
                        ((OAuth2AuthenticationException) exception).getError().getErrorCode())
                .isEqualTo(OidcAccountValidator.INVALID_OIDC_ACCOUNT);
    }

    @Test
    void 관리자_이메일은_호스팅_도메인_제한에서_제외한다() {
        OidcUser oidcUser = createOidcUser("dankook.aegis@gmail.com", true, null);

        assertThatCode(() -> validator.validate(oidcUser)).doesNotThrowAnyException();
    }

    private OidcUser createOidcUser(String email, boolean emailVerified, String hostedDomain) {
        OidcUser oidcUser = mock(OidcUser.class);
        when(oidcUser.getSubject()).thenReturn("oidc-id");
        when(oidcUser.getEmail()).thenReturn(email);
        when(oidcUser.getFullName()).thenReturn("테스트 사용자");
        when(oidcUser.getEmailVerified()).thenReturn(emailVerified);
        when(oidcUser.getClaimAsString("hd")).thenReturn(hostedDomain);
        return oidcUser;
    }
}
