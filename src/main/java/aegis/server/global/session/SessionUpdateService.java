package aegis.server.global.session;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.event.MemberRoleChangedEvent;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.CustomOidcUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionUpdateService {

    @SuppressWarnings("rawtypes")
    private final FindByIndexNameSessionRepository sessionRepository;

    private final MemberRepository memberRepository;

    @SuppressWarnings("unchecked")
    public void updateAuthenticationInAllSessions(Member member) {
        Map<String, ? extends Session> userSessions =
                sessionRepository.findByPrincipalName(member.getId().toString());

        userSessions.values().forEach(session -> {
            updateSecurityContext(session, member);
            sessionRepository.save(session);
            log.info(
                    "[SessionUpdateService] 세션 및 SecurityContext 권한 정보 갱신: memberId={}, sessionId={}, newRole={}",
                    member.getId(),
                    session.getId(),
                    member.getRole());
        });

        log.info(
                "[SessionUpdateService] 사용자의 모든 세션 권한 정보 갱신 완료: memberId={}, 갱신된 세션 수={}",
                member.getId(),
                userSessions.size());
    }

    private void updateSecurityContext(Session session, Member member) {
        SecurityContext securityContext =
                session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (securityContext != null && securityContext.getAuthentication() instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken currentToken = (OAuth2AuthenticationToken) securityContext.getAuthentication();

            // 기존 OidcUser에서 필요한 정보 추출
            OidcUser originalOidcUser = (OidcUser) currentToken.getPrincipal();

            // 새로운 권한으로 CustomOidcUser 생성
            CustomOidcUser updatedOidcUser = new CustomOidcUser(originalOidcUser, member);

            // 새로운 OAuth2AuthenticationToken 생성
            OAuth2AuthenticationToken updatedToken = new OAuth2AuthenticationToken(
                    updatedOidcUser,
                    Collections.singleton(
                            new SimpleGrantedAuthority(member.getRole().getKey())),
                    currentToken.getAuthorizedClientRegistrationId());

            securityContext.setAuthentication(updatedToken);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleMemberRoleChangedEvent(MemberRoleChangedEvent event) {
        Member member = memberRepository
                .findById(event.memberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        updateAuthenticationInAllSessions(member);
    }
}
