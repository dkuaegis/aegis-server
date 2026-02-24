package aegis.server.domain.payment.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Role;
import aegis.server.domain.member.repository.MemberRecordRepository;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.domain.Transaction;
import aegis.server.domain.payment.domain.TransactionType;
import aegis.server.domain.payment.dto.response.AdminPaymentItemResponse;
import aegis.server.domain.payment.dto.response.AdminPaymentPageResponse;
import aegis.server.domain.payment.dto.response.AdminTransactionPageResponse;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.domain.payment.repository.TransactionRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.helper.IntegrationTestWithoutTransactional;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdminPaymentServiceTest extends IntegrationTestWithoutTransactional {

    @Autowired
    AdminPaymentService adminPaymentService;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberRecordRepository memberRecordRepository;

    @Nested
    class 결제_목록_조회 {

        @Test
        void 상태와_회원키워드로_필터링한다() {
            // given
            Member targetMember = createMemberWithName("홍길동");
            Member anotherMember = createMemberWithName("김철수");
            Member oldSemesterMember = createMemberWithName("홍길동");

            paymentRepository.save(Payment.createForDev(targetMember, PaymentStatus.PENDING, CURRENT_YEAR_SEMESTER));
            paymentRepository.save(Payment.createForDev(anotherMember, PaymentStatus.COMPLETED, CURRENT_YEAR_SEMESTER));
            paymentRepository.save(
                    Payment.createForDev(oldSemesterMember, PaymentStatus.PENDING, YearSemester.YEAR_SEMESTER_2025_2));

            // when
            AdminPaymentPageResponse response =
                    adminPaymentService.getPayments(0, 50, CURRENT_YEAR_SEMESTER, PaymentStatus.PENDING, "홍");

            // then
            assertEquals(1, response.totalElements());
            assertEquals(1, response.content().size());
            assertEquals(targetMember.getId(), response.content().get(0).memberId());
            assertEquals(PaymentStatus.PENDING, response.content().get(0).status());
        }
    }

    @Nested
    class 거래_목록_조회 {

        @Test
        void 유형_입금자명_기간으로_필터링한다() {
            // given
            Transaction target = transactionRepository.save(Transaction.of(
                    LocalDateTime.of(2026, 1, 10, 10, 0),
                    "홍길동",
                    TransactionType.DEPOSIT,
                    BigDecimal.valueOf(15000),
                    BigDecimal.valueOf(100000)));

            Transaction otherType = transactionRepository.save(Transaction.of(
                    LocalDateTime.of(2026, 1, 10, 11, 0),
                    "홍길동",
                    TransactionType.WITHDRAWAL,
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(99000)));

            Transaction otherName = transactionRepository.save(Transaction.of(
                    LocalDateTime.of(2026, 1, 11, 11, 0),
                    "김철수",
                    TransactionType.DEPOSIT,
                    BigDecimal.valueOf(15000),
                    BigDecimal.valueOf(114000)));

            ReflectionTestUtils.setField(otherName, "yearSemester", YearSemester.YEAR_SEMESTER_2025_2);
            transactionRepository.save(otherName);

            // when
            AdminTransactionPageResponse response = adminPaymentService.getTransactions(
                    0,
                    50,
                    CURRENT_YEAR_SEMESTER,
                    TransactionType.DEPOSIT,
                    "홍",
                    LocalDate.of(2026, 1, 10),
                    LocalDate.of(2026, 1, 10));

            // then
            assertEquals(1, response.totalElements());
            assertEquals(1, response.content().size());
            assertEquals(target.getId(), response.content().get(0).transactionId());
            assertEquals(TransactionType.DEPOSIT, response.content().get(0).transactionType());
            assertEquals("홍길동", response.content().get(0).depositorName());

            assertTrue(response.content().stream()
                    .noneMatch(item -> item.transactionId().equals(otherType.getId())));
            assertTrue(response.content().stream()
                    .noneMatch(item -> item.transactionId().equals(otherName.getId())));
        }

        @Test
        void 시작일이_종료일보다_늦으면_실패한다() {
            // when-then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> adminPaymentService.getTransactions(
                            0,
                            50,
                            CURRENT_YEAR_SEMESTER,
                            null,
                            null,
                            LocalDate.of(2026, 1, 11),
                            LocalDate.of(2026, 1, 10)));
            assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        }
    }

    @Nested
    class 결제_강제완료 {

        @Test
        void 성공하면_COMPLETED로_변경되고_후속_이벤트가_반영된다() {
            // given
            Member member = createMember();
            member.demoteToGuest();
            memberRepository.save(member);

            Payment payment =
                    paymentRepository.save(Payment.createForDev(member, PaymentStatus.PENDING, CURRENT_YEAR_SEMESTER));

            // when
            AdminPaymentItemResponse response = adminPaymentService.forceCompletePayment(payment.getId());

            // then
            Payment updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
            Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();

            assertEquals(PaymentStatus.COMPLETED, response.status());
            assertEquals(PaymentStatus.COMPLETED, updatedPayment.getStatus());
            assertEquals(Role.USER, updatedMember.getRole());
            assertTrue(memberRecordRepository.existsByMemberIdAndYearSemester(member.getId(), CURRENT_YEAR_SEMESTER));
        }

        @Test
        void 이미_COMPLETED_결제면_실패한다() {
            // given
            Member member = createMember();
            Payment payment = paymentRepository.save(
                    Payment.createForDev(member, PaymentStatus.COMPLETED, CURRENT_YEAR_SEMESTER));

            // when-then
            CustomException exception = assertThrows(
                    CustomException.class, () -> adminPaymentService.forceCompletePayment(payment.getId()));
            assertEquals(ErrorCode.PAYMENT_ALREADY_COMPLETED, exception.getErrorCode());
        }

        @Test
        void 결제가_없으면_실패한다() {
            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> adminPaymentService.forceCompletePayment(9999999L));
            assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
        }
    }

    private Member createMemberWithName(String name) {
        String uniqueId = String.valueOf(System.nanoTime());
        Member member = Member.create(uniqueId, "test" + uniqueId + "@dankook.ac.kr", name);
        member.updatePersonalInfo(
                "010-1234-5678",
                "3200000" + uniqueId.substring(uniqueId.length() - 1),
                aegis.server.domain.member.domain.Department.SW융합대학_컴퓨터공학과,
                aegis.server.domain.member.domain.Grade.THREE,
                "010101",
                aegis.server.domain.member.domain.Gender.MALE);
        member.promoteToUser();
        return memberRepository.save(member);
    }
}
