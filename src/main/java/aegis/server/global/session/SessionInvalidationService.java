package aegis.server.global.session;

import java.util.Map;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionInvalidationService {

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    public void invalidateAllUserSessions(Long memberId) {
        Map<String, ? extends Session> userSessions = sessionRepository.findByPrincipalName(memberId.toString());

        userSessions.values().forEach(session -> {
            sessionRepository.deleteById(session.getId());
            log.info("[SessionInvalidationService] 세션 무효화: memberId={}, sessionId={}", memberId, session.getId());
        });

        log.info(
                "[SessionInvalidationService] 사용자의 모든 세션 무효화 완료: memberId={}, 무효화된 세션 수={}",
                memberId,
                userSessions.size());
    }
}
