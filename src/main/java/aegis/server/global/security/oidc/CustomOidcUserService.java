package aegis.server.global.security.oidc;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOidcUserService extends OidcUserService {

    private final MemberRepository memberRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        Member member = findOrSave(oidcUser);

        return new CustomOidcUser(oidcUser, member);
    }

    private Member findOrSave(OidcUser oidcUser) {
        String oidcId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        if (email == null || !email.endsWith("@dankook.ac.kr")) {
            throw new OAuth2AuthenticationException("단국대학교 구성원만 로그인할 수 있습니다.");
        }

        return memberRepository.findByOidcId(oidcId).orElseGet(
                () -> memberRepository.save(Member.createGuestMember(oidcId, email, name))
        );
    }
}
