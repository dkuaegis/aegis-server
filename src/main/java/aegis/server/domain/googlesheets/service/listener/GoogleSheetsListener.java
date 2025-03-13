package aegis.server.domain.googlesheets.service.listener;

import aegis.server.domain.googlesheets.service.GoogleSheetsService;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Student;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.member.repository.StudentRepository;
import aegis.server.domain.payment.domain.event.PaymentCompletedEvent;
import aegis.server.domain.payment.dto.internal.PaymentInfo;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Profile("!test")
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleSheetsListener {

    private final GoogleSheetsService googleSheetsService;
    private final MemberRepository memberRepository;
    private final StudentRepository studentRepository;

    @Async("googleSheetsTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW, readOnly = true)
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        PaymentInfo paymentInfo = event.paymentInfo();
        
        log.info(
            "[GoogleSheetsSyncListener][PaymentCompletedEvent] Google Sheets 회원 등록 처리 시작: paymentId={}",
            paymentInfo.id()
        );
        
        try {
            Member member = memberRepository.findById(paymentInfo.memberId())
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

            Student student = studentRepository.findById(paymentInfo.studentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

            googleSheetsService.addMemberRegistration(member, student, paymentInfo);

            log.info(
                    "[GoogleSheetsSyncListener][PaymentCompletedEvent] Google Sheets 회원 등록 정보 추가 완료: paymentId={}, memberId={}, name={}",
                    paymentInfo.id(),
                    paymentInfo.memberId(),
                    member.getName()
            );
        } catch (IOException e) {
            log.error(
                    "[GoogleSheetsSyncListener][PaymentCompletedEvent] Google Sheets 회원 등록 정보 추가 실패: paymentId={}, error={}",
                    paymentInfo.id(),
                    e.getMessage()
            );
        } catch (Exception e) {
            log.error(
                    "[GoogleSheetsSyncListener][PaymentCompletedEvent] 예상치 못한 오류 발생: paymentId={}, error={}",
                    paymentInfo.id(),
                    e.getMessage(),
                    e
            );
        }
    }
}
