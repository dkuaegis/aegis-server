package aegis.server.domain.payment.service;

import aegis.server.domain.coupon.domain.Coupon;
import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Student;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.dto.request.PaymentRequest;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static aegis.server.global.constant.Constant.CLUB_DUES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PaymentServiceTest extends IntegrationTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    IssuedCouponRepository issuedCouponRepository;

    private Member member;
    private Student student;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        member = createMember();
        student = createStudent(member);
        userDetails = createUserDetails(member);
    }

    @Nested
    class 결제정보_생성 {

        @Test
        void 성공한다() {
            // given
            PaymentRequest request = new PaymentRequest(List.of());

            // when
            paymentService.createOrUpdatePendingPayment(request, userDetails);

            // then
            Payment payment = paymentRepository.findByStudentInCurrentYearSemester(student).get();
            assertEquals(student.getId(), payment.getStudent().getId());
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
            assertEquals(CLUB_DUES, payment.getFinalPrice());
        }

        @Test
        void 쿠폰_적용_시_할인된_가격이_적용된다() {
            // given
            Coupon coupon = createCoupon();
            IssuedCoupon issuedCoupon = createIssuedCoupon(member, coupon);
            PaymentRequest request = new PaymentRequest(List.of(issuedCoupon.getId()));

            // when
            paymentService.createOrUpdatePendingPayment(request, userDetails);

            // then
            Payment payment = paymentRepository.findByStudentInCurrentYearSemester(student).get();
            BigDecimal discountedPrice = CLUB_DUES.subtract(coupon.getDiscountAmount());
            assertEquals(discountedPrice, payment.getFinalPrice());
        }

        @Test
        void 쿠폰_적용_시_사용_처리_된다() {
            // given
            Coupon coupon = createCoupon();
            IssuedCoupon issuedCoupon = createIssuedCoupon(member, coupon);
            PaymentRequest request = new PaymentRequest(List.of(issuedCoupon.getId()));

            // when
            paymentService.createOrUpdatePendingPayment(request, userDetails);

            // then
            IssuedCoupon updatedIssuedCoupon = issuedCouponRepository.findById(issuedCoupon.getId()).get();
            assertEquals(false, updatedIssuedCoupon.getIsValid());
        }

        @Test
        void 본인에게_발급되지_않은_쿠폰_사용_시_실패한다() {
            // given
            Coupon coupon = createCoupon();
            Member anotherMember = createMember();
            IssuedCoupon issuedCoupon = createIssuedCoupon(anotherMember, coupon);
            PaymentRequest request = new PaymentRequest(List.of(issuedCoupon.getId()));

            // when-then
            CustomException exception = assertThrows(CustomException.class,
                    () -> paymentService.createOrUpdatePendingPayment(request, userDetails));
            assertEquals(ErrorCode.ISSUED_COUPON_NOT_FOUND_FOR_MEMBER, exception.getErrorCode());
            IssuedCoupon shouldNotBeUpdatedIssuedCoupon = issuedCouponRepository.findById(issuedCoupon.getId()).get();
            assertEquals(true, shouldNotBeUpdatedIssuedCoupon.getIsValid());
        }

        @Test
        void 중복된_결제정보_생성_시_기존_정보를_덮어씌운다() {
            // given
            PaymentRequest oldRequest = new PaymentRequest(List.of());

            paymentService.createOrUpdatePendingPayment(oldRequest, userDetails);

            // when
            Coupon coupon = createCoupon();
            createIssuedCoupon(member, coupon);
            PaymentRequest newRequest = new PaymentRequest(List.of(1L));
            paymentService.createOrUpdatePendingPayment(newRequest, userDetails);
            Payment secondPayment = paymentRepository.findById(1L).get();

            // then
            assertEquals(PaymentStatus.PENDING, secondPayment.getStatus());
            assertEquals(CLUB_DUES.subtract(coupon.getDiscountAmount()), secondPayment.getFinalPrice());
        }

        @Test
        @Transactional
        void 완료된_결제정보가_존재하면_실패한다() {
            // given
            PaymentRequest request = new PaymentRequest(List.of());

            Payment payment = Payment.of(student);
            ReflectionTestUtils.setField(payment, "status", PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            // when-then
            CustomException exception = assertThrows(CustomException.class,
                    () -> paymentService.createOrUpdatePendingPayment(request, userDetails));
            assertEquals(ErrorCode.PAYMENT_ALREADY_COMPLETED, exception.getErrorCode());
        }

        @Test
        @Transactional
        void 초과입금된_결제정보가_존재하면_실패한다() {
            // given
            PaymentRequest request = new PaymentRequest(List.of());

            Payment payment = Payment.of(student);
            ReflectionTestUtils.setField(payment, "status", PaymentStatus.OVERPAID);
            paymentRepository.save(payment);

            // when-then
            CustomException exception = assertThrows(CustomException.class,
                    () -> paymentService.createOrUpdatePendingPayment(request, userDetails));
            assertEquals(ErrorCode.PAYMENT_ALREADY_OVER_PAID, exception.getErrorCode());
        }
    }
}
