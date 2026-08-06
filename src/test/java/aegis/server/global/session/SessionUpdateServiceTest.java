package aegis.server.global.session;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
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
import aegis.server.domain.member.dto.response.MemberDemoteResponse;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.member.service.MemberService;
import aegis.server.domain.member.service.listener.MemberEventListener;
import aegis.server.domain.payment.domain.event.PaymentCompletedEvent;
import aegis.server.domain.payment.dto.internal.PaymentInfo;
import aegis.server.global.security.oidc.CustomOidcUser;
import aegis.server.helper.IntegrationTestWithoutTransactional;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"rawtypes", "unchecked"})
class SessionUpdateServiceTest extends IntegrationTestWithoutTransactional {

    @Autowired
    SessionUpdateService sessionUpdateService;

    @Autowired
    MemberService memberService;

    @Autowired
    MemberEventListener memberEventListener;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FindByIndexNameSessionRepository sessionRepository;

    @Test
    void 회원의_모든_세션_정보를_갱신한다() {
        // given
        Member member = createMember();
        member.demoteToGuest();
        memberRepository.saveAndFlush(member);

        Session session = createSession(member);
        assertTrue(
                sessionRepository.findByPrincipalName(member.getId().toString()).containsKey(session.getId()));

        member.promoteToUser();
        memberRepository.saveAndFlush(member);

        // when
        sessionUpdateService.updateAuthenticationInAllSessions(member);

        // then
        assertSessionRole(session.getId(), Role.USER);
    }

    @Test
    void 결제_완료로_승격되면_세션_권한도_USER로_갱신한다() {
        // given
        Member member = createMember();
        member.demoteToGuest();
        memberRepository.save(member);
        Session session = createSession(member);

        LocalDateTime now = LocalDateTime.now();
        PaymentCompletedEvent event = new PaymentCompletedEvent(
                new PaymentInfo(1L, member.getId(), CURRENT_YEAR_SEMESTER, BigDecimal.ZERO, now, now));

        // when
        memberEventListener.handlePaymentCompletedEvent(event);

        // then
        Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();
        assertEquals(Role.USER, updatedMember.getRole());
        assertSessionRole(session.getId(), Role.USER);
    }

    @Test
    void 일괄_강등되면_세션_권한도_GUEST로_갱신한다() {
        // given
        Member member = createMember();
        Session session = createSession(member);

        // when
        MemberDemoteResponse response = memberService.demoteMembersForCurrentSemester();

        // then
        Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();
        assertEquals(Role.GUEST, updatedMember.getRole());
        assertTrue(response.demotedMemberStudentIds().contains(member.getStudentId()));
        assertSessionRole(session.getId(), Role.GUEST);
    }

    private Session createSession(Member member) {
        Session session = sessionRepository.createSession();
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, createSecurityContext(member));
        sessionRepository.save(session);
        return session;
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

    private void assertSessionRole(String sessionId, Role expectedRole) {
        Session session = (Session) sessionRepository.findById(sessionId);
        assertNotNull(session);

        SecurityContext securityContext =
                session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) securityContext.getAuthentication();
        CustomOidcUser principal = (CustomOidcUser) authentication.getPrincipal();

        assertEquals(expectedRole, principal.getUserDetails().getRole());
        assertEquals(1, authentication.getAuthorities().size());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority(expectedRole.getKey())));
    }
}
