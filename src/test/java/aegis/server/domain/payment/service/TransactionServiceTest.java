package aegis.server.domain.payment.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.coupon.domain.Coupon;
import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.coupon.repository.CouponRepository;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.domain.member.domain.Department;
import aegis.server.domain.member.domain.Gender;
import aegis.server.domain.member.domain.Grade;
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

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    IssuedCouponRepository issuedCouponRepository;

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

        @Test
        void 결제완료_시_적용된_쿠폰이_사용처리된다() {
            // given
            Member member = createMember();
            UserDetails userDetails = UserDetails.from(member);
            Coupon coupon = couponRepository.save(Coupon.create("테스트쿠폰", BigDecimal.valueOf(5000L)));
            IssuedCoupon issuedCoupon = issuedCouponRepository.save(IssuedCoupon.of(coupon, member));

            paymentService.createPayment(new PaymentRequest(List.of(issuedCoupon.getId())), userDetails);

            // 사전 상태 확인
            IssuedCoupon before =
                    issuedCouponRepository.findById(issuedCoupon.getId()).get();
            assertEquals(true, before.getIsValid());

            BigDecimal finalPrice = CLUB_DUES.subtract(coupon.getDiscountAmount());
            String transactionLog = String.format(DEPOSIT_TRANSACTION_LOG_FORMAT, finalPrice, member.getName());

            // when
            transactionService.createTransaction(transactionLog);

            // then
            IssuedCoupon after =
                    issuedCouponRepository.findById(issuedCoupon.getId()).get();
            Payment payment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            assertEquals(false, after.getIsValid());
            assertEquals(payment.getId(), after.getPayment().getId());
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

    @Nested
    class 동명이인_결제 {

        @Test
        void 동명이인이_있는_경우_두_결제_모두_PENDING_상태를_유지한다() {
            // given
            Member member1 = createMemberWithName("홍길동");
            Member member2 = createMemberWithName("홍길동");

            UserDetails userDetails1 = UserDetails.from(member1);
            UserDetails userDetails2 = UserDetails.from(member2);

            PaymentRequest request = new PaymentRequest(List.of());
            paymentService.createPayment(request, userDetails1);
            paymentService.createPayment(request, userDetails2);

            String transactionLog = String.format(DEPOSIT_TRANSACTION_LOG_FORMAT, CLUB_DUES, "홍길동");

            // when
            transactionService.createTransaction(transactionLog);

            // then
            Payment payment1 =
                    paymentRepository.findByMemberInCurrentYearSemester(member1).get();
            Payment payment2 =
                    paymentRepository.findByMemberInCurrentYearSemester(member2).get();
            assertEquals(PaymentStatus.PENDING, payment1.getStatus());
            assertEquals(PaymentStatus.PENDING, payment2.getStatus());
        }

        private Member createMemberWithName(String name) {
            String uniqueId = String.valueOf(System.nanoTime());
            Member member = Member.create(uniqueId, "test" + uniqueId + "@dankook.ac.kr", name);
            member.updatePersonalInfo(
                    "010-1234-5678", "32000001", Department.SW융합대학_컴퓨터공학과, Grade.THREE, "010101", Gender.MALE);
            member.promoteToUser();
            return memberRepository.save(member);
        }
    }
}
