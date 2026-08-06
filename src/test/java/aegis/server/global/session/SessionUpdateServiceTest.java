package aegis.server.global.session;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Role;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.global.security.oidc.CustomOidcUser;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SessionUpdateServiceTest extends IntegrationTest {

    @Autowired
    SessionUpdateService sessionUpdateService;

    @Autowired
    MemberRepository memberRepository;

    @SuppressWarnings("rawtypes")
    @Autowired
    FindByIndexNameSessionRepository sessionRepository;

    @Test
    @SuppressWarnings("unchecked")
    void 회원의_모든_세션_정보를_갱신한다() {
        // given
        Member member = createMember();
        member.demoteToGuest();
        memberRepository.saveAndFlush(member);

        Session session = sessionRepository.createSession();
        session.setAttribute(
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                member.getId().toString());
        session.setAttribute("userDetails", UserDetails.from(member));
        session.setAttribute("SPRING_SECURITY_CONTEXT", createSecurityContext(member));
        sessionRepository.save(session);

        member.promoteToUser();
        memberRepository.saveAndFlush(member);

        // when
        sessionUpdateService.updateUserDetailsInAllSessions(member);

        // then
        Session updatedSession = (Session) sessionRepository.findById(session.getId());
        assertNotNull(updatedSession);
        UserDetails updatedUserDetails = updatedSession.getAttribute("userDetails");
        assertEquals(Role.USER, updatedUserDetails.getRole());

        SecurityContext updatedSecurityContext = updatedSession.getAttribute("SPRING_SECURITY_CONTEXT");
        CustomOidcUser updatedPrincipal =
                (CustomOidcUser) updatedSecurityContext.getAuthentication().getPrincipal();
        assertEquals(Role.USER, updatedPrincipal.getUserDetails().getRole());
    }

    private SecurityContext createSecurityContext(Member member) {
        Instant now = Instant.now();
        Map<String, Object> claims = Map.of("sub", member.getOidcId());
        OidcUser oidcUser = new DefaultOidcUser(
                List.of(new SimpleGrantedAuthority(member.getRole().getKey())),
                new OidcIdToken("test-token", now, now.plusSeconds(300), claims),
                new OidcUserInfo(claims));
        CustomOidcUser customOidcUser = new CustomOidcUser(oidcUser, member);
        OAuth2AuthenticationToken authentication =
                new OAuth2AuthenticationToken(customOidcUser, customOidcUser.getAuthorities(), "google");
        return new SecurityContextImpl(authentication);
    }
}
