package aegis.server.domain.payment.service;

import aegis.server.common.IntegrationTest;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.dto.request.PaymentRequest;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.global.security.dto.SessionUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static aegis.server.domain.payment.domain.Payment.expectedDepositorName;
import static aegis.server.global.constant.Constant.CLUB_DUES;
import static aegis.server.global.constant.Constant.CURRENT_SEMESTER;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionServiceTest extends IntegrationTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    TransactionService transactionService;

    Member member;

    @BeforeEach
    void setUp() {
        member = createMember();
        SessionUser sessionUser = createSessionUser(member);
        PaymentRequest request = new PaymentRequest(List.of());
        paymentService.createPendingPayment(request, sessionUser);
    }

    @Nested
    class 올바른_입금 {

        @Test
        void 결제를_COMPLETE_처리한다() {
            // given
            String transactionLog = String.format("""
                    [입금] %s원 %s
                    982-******-01-017
                    01/09 12:25 / 잔액 1000000원
                    """, CLUB_DUES, expectedDepositorName(member));

            // when
            transactionService.createTransaction(transactionLog);

            // then
            Payment payment = paymentRepository.findByMemberAndCurrentSemester(member, CURRENT_SEMESTER).orElseThrow();
            assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
            assertEquals(CLUB_DUES, getCurrentCurrentDepositAmount(payment.getExpectedDepositorName()));
        }
    }

    @Nested
    class 잘못된_입금 {

        @Test
        void 틀린_입금자명() {
            // given
            String transactionLog = String.format("""
                    [입금] %s원 %s
                    982-******-01-017
                    01/09 12:25 / 잔액 1000000원
                    """, CLUB_DUES, expectedDepositorName(member) + "WRONG");

            // when
            transactionService.createTransaction(transactionLog);

            // then
            Payment payment = paymentRepository.findByMemberAndCurrentSemester(member, CURRENT_SEMESTER).orElseThrow();
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
            assertEquals(BigDecimal.ZERO, getCurrentCurrentDepositAmount(payment.getExpectedDepositorName()));
        }

        @Test
        void 부족한_입금액() {
            // given
            String transactionLog = String.format("""
                    [입금] %s원 %s
                    982-******-01-017
                    01/09 12:25 / 잔액 1000000원
                    """, CLUB_DUES.subtract(BigDecimal.ONE), expectedDepositorName(member));

            // when
            transactionService.createTransaction(transactionLog);

            // then
            Payment payment = paymentRepository.findByMemberAndCurrentSemester(member, CURRENT_SEMESTER).orElseThrow();
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
            assertEquals(CLUB_DUES.subtract(BigDecimal.ONE), getCurrentCurrentDepositAmount(payment.getExpectedDepositorName()));
        }

        @Test
        void 초과된_입금액() {
            // given
            String transactionLog = String.format("""
                    [입금] %s원 %s
                    982-******-01-017
                    01/09 12:25 / 잔액 1000000원
                    """, CLUB_DUES.add(BigDecimal.ONE), expectedDepositorName(member));

            // when
            transactionService.createTransaction(transactionLog);

            // then
            Payment payment = paymentRepository.findByMemberAndCurrentSemester(member, CURRENT_SEMESTER).orElseThrow();
            assertEquals(PaymentStatus.OVERPAID, payment.getStatus());
            assertEquals(CLUB_DUES.add(BigDecimal.ONE), getCurrentCurrentDepositAmount(payment.getExpectedDepositorName()));
        }
    }

    @Nested
    class 다중입금 {

        @Test
        void 올바른_추가입금() {
            // given
            String transactionLog1 = String.format("""
                    [입금] %s원 %s
                    982-******-01-017
                    01/09 12:25 / 잔액 1000000원
                    """, CLUB_DUES.subtract(BigDecimal.ONE), expectedDepositorName(member));
            transactionService.createTransaction(transactionLog1);

            // when
            String transactionLog2 = String.format("""
                    [입금] %s원 %s
                    982-******-01-017
                    01/09 12:25 / 잔액 1000000원
                    """, BigDecimal.ONE, expectedDepositorName(member));
            transactionService.createTransaction(transactionLog2);

            // then
            Payment payment = paymentRepository.findByMemberAndCurrentSemester(member, CURRENT_SEMESTER).orElseThrow();
            assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        }

        @Test
        void 초과된_추가입금() {
            // given
            String transactionLog1 = String.format("""
                    [입금] %s원 %s
                    982-******-01-017
                    01/09 12:25 / 잔액 1000000원
                    """, CLUB_DUES.subtract(BigDecimal.ONE), expectedDepositorName(member));
            transactionService.createTransaction(transactionLog1);

            // when
            String transactionLog2 = String.format("""
                    [입금] %s원 %s
                    982-******-01-017
                    01/09 12:25 / 잔액 1000000원
                    """, BigDecimal.TWO, expectedDepositorName(member));
            transactionService.createTransaction(transactionLog2);

            // then
            Payment payment = paymentRepository.findByMemberAndCurrentSemester(member, CURRENT_SEMESTER).orElseThrow();
            assertEquals(PaymentStatus.OVERPAID, payment.getStatus());
        }
    }
}
