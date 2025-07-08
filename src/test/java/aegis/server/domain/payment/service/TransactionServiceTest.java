package aegis.server.domain.payment.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.dto.request.PaymentRequest;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.domain.payment.repository.TransactionRepository;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static aegis.server.global.constant.Constant.CLUB_DUES;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionServiceTest extends IntegrationTest {

    @Autowired
    TransactionService transactionService;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    private final String TRANSACTION_LOG_FORMAT =
            """
            [입금] %s원 %s
            982-******-01-017
            01/09 12:25 / 잔액 1000000원
            """;

    private Member member;
    private String expectedDepositorName;

    @BeforeEach
    void setUp() {
        member = createMember();
        expectedDepositorName = member.getName();
        UserDetails userDetails = UserDetails.from(member);
        PaymentRequest request = new PaymentRequest(List.of());
        paymentService.createOrUpdatePendingPayment(request, userDetails);
    }

    @Nested
    class 올바른_입금 {

        @Test
        void 결제를_COMPLETED_처리한다() {
            // given
            String transactionLog = String.format(TRANSACTION_LOG_FORMAT, CLUB_DUES, expectedDepositorName);

            // when
            transactionService.createTransaction(transactionLog);

            // then
            Payment payment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        }
    }

    @Nested
    class 잘못된_입금 {

        @Test
        void 잘못된_입금자명() {
            // given
            String transactionLog = String.format(TRANSACTION_LOG_FORMAT, CLUB_DUES, expectedDepositorName + "WRONG");

            // when
            transactionService.createTransaction(transactionLog);

            // then
            Payment payment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
            assertEquals(
                    BigDecimal.ZERO,
                    transactionRepository.sumAmountByDepositorName(payment.getExpectedDepositorName()));
        }

        @Test
        void 부족한_입금액() {
            // given
            String transactionLog =
                    String.format(TRANSACTION_LOG_FORMAT, CLUB_DUES.subtract(BigDecimal.ONE), expectedDepositorName);

            // when
            transactionService.createTransaction(transactionLog);

            // then
            Payment payment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
            assertEquals(
                    CLUB_DUES.subtract(BigDecimal.ONE),
                    transactionRepository.sumAmountByDepositorName(payment.getExpectedDepositorName()));
        }

        @Test
        void 초과된_입금액() {
            // given
            String transactionLog =
                    String.format(TRANSACTION_LOG_FORMAT, CLUB_DUES.add(BigDecimal.ONE), expectedDepositorName);

            // when
            transactionService.createTransaction(transactionLog);

            // then
            Payment payment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            assertEquals(PaymentStatus.OVERPAID, payment.getStatus());
            assertEquals(
                    CLUB_DUES.add(BigDecimal.ONE),
                    transactionRepository.sumAmountByDepositorName(payment.getExpectedDepositorName()));
        }
    }

    @Nested
    class 다중입금 {

        @Test
        void 올바른_추가입금() {
            // given
            String transactionLog1 =
                    String.format(TRANSACTION_LOG_FORMAT, CLUB_DUES.subtract(BigDecimal.ONE), expectedDepositorName);
            transactionService.createTransaction(transactionLog1);

            // when
            String transactionLog2 = String.format(TRANSACTION_LOG_FORMAT, BigDecimal.ONE, expectedDepositorName);
            transactionService.createTransaction(transactionLog2);

            // then
            Payment payment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        }

        @Test
        void 초과된_추가입금() {
            // given
            String transactionLog1 =
                    String.format(TRANSACTION_LOG_FORMAT, CLUB_DUES.subtract(BigDecimal.ONE), expectedDepositorName);
            transactionService.createTransaction(transactionLog1);

            // when
            String transactionLog2 = String.format(TRANSACTION_LOG_FORMAT, BigDecimal.TWO, expectedDepositorName);
            transactionService.createTransaction(transactionLog2);

            // then
            Payment payment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            assertEquals(PaymentStatus.OVERPAID, payment.getStatus());
            assertEquals(
                    CLUB_DUES.add(BigDecimal.ONE),
                    transactionRepository.sumAmountByDepositorName(payment.getExpectedDepositorName()));
        }
    }
}
