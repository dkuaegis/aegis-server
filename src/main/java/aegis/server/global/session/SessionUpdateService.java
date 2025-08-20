package aegis.server.global.session;

import java.util.Collections;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
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
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.payment.domain.event.PaymentCompletedEvent;
import aegis.server.domain.payment.dto.internal.PaymentInfo;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.CustomOidcUser;
import aegis.server.global.security.oidc.UserDetails;

@Profile("!test")
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionUpdateService {

    @SuppressWarnings("rawtypes")
    private final FindByIndexNameSessionRepository sessionRepository;

    private final MemberRepository memberRepository;

    @SuppressWarnings("unchecked")
    public void updateUserDetailsInAllSessions(Member member) {
        Map<String, ? extends Session> userSessions =
                sessionRepository.findByPrincipalName(member.getId().toString());

        UserDetails updatedUserDetails = UserDetails.from(member);

        userSessions.values().forEach(session -> {
            // 1. 세션의 userDetails 갱신
            session.setAttribute("userDetails", updatedUserDetails);

            // 2. SecurityContext의 Authentication 갱신
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
        SecurityContext securityContext = session.getAttribute("SPRING_SECURITY_CONTEXT");
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
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        PaymentInfo paymentInfo = event.paymentInfo();

        Member member = memberRepository
                .findById(paymentInfo.memberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 결제가 완료되는 경우 사용자의 권한이 변경될 수 있으므로 세션의 권한 정보를 갱신합니다.
        updateUserDetailsInAllSessions(member);
    }
}
