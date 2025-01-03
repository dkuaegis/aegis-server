package aegis.server.global.security.oidc;

import aegis.server.domain.member.domain.Member;
import lombok.Getter;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Getter
public class CustomOidcUser extends DefaultOidcUser {

    private final UserAuthInfo userAuthInfo;

    public CustomOidcUser(OidcUser oidcUser, Member member) {
        super(oidcUser.getAuthorities(), oidcUser.getIdToken(), oidcUser.getUserInfo());
        this.userAuthInfo = UserAuthInfo.from(member);
    }
}
