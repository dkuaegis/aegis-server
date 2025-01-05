package aegis.server.global.security.oidc;

import aegis.server.domain.member.domain.Member;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collections;

@Getter
public class CustomOidcUser extends DefaultOidcUser {

    private final UserAuthInfo userAuthInfo;

    public CustomOidcUser(OidcUser oidcUser, Member member) {
        super(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().getKey())),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo()
        );
        this.userAuthInfo = UserAuthInfo.from(member);
    }
}
