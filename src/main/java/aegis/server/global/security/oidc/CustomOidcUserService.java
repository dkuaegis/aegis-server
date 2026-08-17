package aegis.server.global.security.oidc;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.repository.PointAccountRepository;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final MemberRepository memberRepository;
    private final PointAccountRepository pointAccountRepository;
    private final OidcAccountValidator oidcAccountValidator;
    private final OidcUserService delegate = new OidcUserService();

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);
        oidcAccountValidator.validate(oidcUser);

        Member member = findOrCreateMember(oidcUser);
        createPointAccountIfNotExists(member);

        return new CustomOidcUser(oidcUser, member);
    }

    private Member findOrCreateMember(OidcUser oidcUser) {
        String oidcId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        Member member = memberRepository.findByOidcId(oidcId).orElse(null);
        if (member == null) {
            return memberRepository.save(Member.create(oidcId, email, name));
        } else {
            if (!member.getEmail().equals(email)) {
                member.updateEmail(email);
            }
            if (!member.getName().equals(name)) {
                member.updateName(name);
            }
        }

        return member;
    }

    private void createPointAccountIfNotExists(Member member) {
        if (!pointAccountRepository.existsByMember(member)) {
            pointAccountRepository.save(PointAccount.create(member));
        }
    }
}
