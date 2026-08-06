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
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Role;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.global.security.oidc.CustomOidcUser;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, createSecurityContext(member));
        sessionRepository.save(session);
        assertTrue(
                sessionRepository.findByPrincipalName(member.getId().toString()).containsKey(session.getId()));

        member.promoteToUser();
        memberRepository.saveAndFlush(member);

        // when
        sessionUpdateService.updateAuthenticationInAllSessions(member);

        // then
        Session updatedSession = (Session) sessionRepository.findById(session.getId());
        assertNotNull(updatedSession);
        SecurityContext updatedSecurityContext =
                updatedSession.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
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
