package aegis.server.domain.member.service.listener;

import aegis.server.domain.member.domain.JoinProgress;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.payment.domain.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberEventListener {

    private final MemberRepository memberRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handelPaymentCompletedEvent(PaymentCompletedEvent event) {
        Member member = event.payment().getMember();
        member.updateJoinProgress(JoinProgress.COMPLETE);
        // AFTER_COMMIT 이벤트이므로 member 객체는 영속성 컨텍스트에 존재하지 않으므로 명시적으로 save를 호출해야 함
        memberRepository.save(member);
        log.info(
                "[MemberEventListener][PaymentCompletedEvent] 회원 가입 완료 처리: memberId={}, paymentId={}",
                member.getId(),
                event.payment().getId()
        );
    }
}
