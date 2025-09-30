package aegis.server.global.security.oidc;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

import aegis.server.domain.member.domain.Member;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomOidcUser extends DefaultOidcUser {

    private final UserDetails userDetails;

    // Jackson용 생성자
    @JsonCreator
    public CustomOidcUser(
            @JsonProperty("authorities") Collection<? extends GrantedAuthority> authorities,
            @JsonProperty("idToken") OidcIdToken idToken,
            @JsonProperty("userInfo") OidcUserInfo userInfo,
            @JsonProperty("userDetails") UserDetails userDetails) {
        super(authorities, idToken, userInfo);
        this.userDetails = userDetails;
    }

    // 애플리케이션용 생성 경로
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
