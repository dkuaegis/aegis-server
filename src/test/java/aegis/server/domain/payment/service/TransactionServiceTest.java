package aegis.server.domain.payment.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Role;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.dto.request.PaymentRequest;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.domain.payment.repository.TransactionRepository;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTestWithoutTransactional;

import static aegis.server.global.constant.Constant.CLUB_DUES;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionServiceTest extends IntegrationTestWithoutTransactional {

    @Autowired
    TransactionService transactionService;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    MemberRepository memberRepository;

    private final String DEPOSIT_TRANSACTION_LOG_FORMAT =
            """
            [입금] %s원 %s
            982-******-01-017
            01/09 12:25 / 잔액 1000000원
            """;

    private final String WITHDRAWAL_TRANSACTION_LOG_FORMAT =
            """
            [출금] %s원 %s
            982-******-01-017
            01/09 12:25 / 잔액 1000000원
            """;

    @Nested
    class 올바른_입금 {

        @Test
        void 결제를_COMPLETED_처리한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = UserDetails.from(member);
            PaymentRequest request = new PaymentRequest(List.of());
            paymentService.createPayment(request, userDetails);

            String transactionLog = String.format(DEPOSIT_TRANSACTION_LOG_FORMAT, CLUB_DUES, member.getName());

            // when
            transactionService.createTransaction(transactionLog);

            // then
            Payment payment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        }
    }

    @Nested
    class 틀린_입금 {

        @Test
        void 잘못된_입금자명() {
            // given
            Member member = createMember();
            UserDetails userDetails = UserDetails.from(member);
            PaymentRequest request = new PaymentRequest(List.of());
            paymentService.createPayment(request, userDetails);

            String transactionLog =
                    String.format(DEPOSIT_TRANSACTION_LOG_FORMAT, CLUB_DUES, member.getName() + "WRONG");

            // when
            transactionService.createTransaction(transactionLog);

            // then
            Payment payment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
        }

        @Test
        void 틀린_입금액() {
            // given
            Member member = createMember();
            UserDetails userDetails = UserDetails.from(member);
            PaymentRequest request = new PaymentRequest(List.of());
            paymentService.createPayment(request, userDetails);

            String transactionLog =
                    String.format(DEPOSIT_TRANSACTION_LOG_FORMAT, CLUB_DUES.subtract(BigDecimal.ONE), member.getName());

            // when
            transactionService.createTransaction(transactionLog);

            // then
            Payment payment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
        }
    }

    @Nested
    class 입금_후_승격 {

        @Test
        void GUEST_회원이_입금_완료하면_USER로_승격한다() {
            // given
            Member guestMember = createMember();
            guestMember.demoteToGuest();
            memberRepository.save(guestMember);

            UserDetails userDetails = UserDetails.from(guestMember);
            PaymentRequest request = new PaymentRequest(List.of());
            paymentService.createPayment(request, userDetails);

            String transactionLog = String.format(DEPOSIT_TRANSACTION_LOG_FORMAT, CLUB_DUES, guestMember.getName());

            // when
            transactionService.createTransaction(transactionLog);

            // then
            Member updatedMember =
                    memberRepository.findById(guestMember.getId()).get();
            assertEquals(Role.USER, updatedMember.getRole());
        }

        @Test
        void USER_회원은_입금_완료해도_역할이_변하지_않는다() {
            // given
            Member userMember = createMember();
            userMember.promoteToUser();
            memberRepository.save(userMember);

            UserDetails userDetails = UserDetails.from(userMember);
            PaymentRequest request = new PaymentRequest(List.of());
            paymentService.createPayment(request, userDetails);

            String transactionLog = String.format(DEPOSIT_TRANSACTION_LOG_FORMAT, CLUB_DUES, userMember.getName());

            // when
            transactionService.createTransaction(transactionLog);

            // then
            Member updatedMember = memberRepository.findById(userMember.getId()).get();
            assertEquals(Role.USER, updatedMember.getRole());
        }

        @Test
        void ADMIN_회원은_입금_완료해도_역할이_변하지_않는다() {
            // given
            Member adminMember = createMember();
            ReflectionTestUtils.setField(adminMember, "role", Role.ADMIN);
            memberRepository.save(adminMember);

            UserDetails userDetails = UserDetails.from(adminMember);
            PaymentRequest request = new PaymentRequest(List.of());
            paymentService.createPayment(request, userDetails);

            String transactionLog = String.format(DEPOSIT_TRANSACTION_LOG_FORMAT, CLUB_DUES, adminMember.getName());

            // when
            transactionService.createTransaction(transactionLog);

            // then
            Member updatedMember =
                    memberRepository.findById(adminMember.getId()).get();
            assertEquals(Role.ADMIN, updatedMember.getRole());
        }
    }

    @Nested
    class 출금_거래 {

        @Test
        void 출금_거래는_거래_정보만_저장된다() {
            // given
            Member member = createMember();
            String transactionLog = String.format(WITHDRAWAL_TRANSACTION_LOG_FORMAT, CLUB_DUES, member.getName());
            int initialTransactionCount = transactionRepository.findAll().size();

            // when
            transactionService.createTransaction(transactionLog);

            // then
            int finalTransactionCount = transactionRepository.findAll().size();
            assertEquals(initialTransactionCount + 1, finalTransactionCount);
        }
    }
}
