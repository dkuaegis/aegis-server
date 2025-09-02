package aegis.server.global.security.oidc;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import lombok.Getter;

import aegis.server.domain.member.domain.Member;

@Getter
public class CustomOidcUser extends DefaultOidcUser {

    private final UserDetails userDetails;

    public CustomOidcUser(OidcUser oidcUser, Member member) {
        super(
                Collections.singleton(
                        new SimpleGrantedAuthority(member.getRole().getKey())),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo());
        this.userDetails = UserDetails.from(member);
    }

    @Override
    public String getName() {
        return userDetails.getMemberId().toString();
    }
}
