package aegis.server.domain.payment.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.coupon.domain.Coupon;
import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.coupon.repository.CouponRepository;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.dto.request.PaymentRequest;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

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

    @Autowired
    CouponRepository couponRepository;

    private Member member;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        member = createMember();
        userDetails = createUserDetails(member);
    }

    @Nested
    class 결제정보_생성 {

        @Test
        void 성공한다() {
            // given
            PaymentRequest request = new PaymentRequest(List.of());

            // when
            paymentService.createPayment(request, userDetails);

            // then
            Payment payment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            assertEquals(member.getId(), payment.getMember().getId());
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
            assertEquals(CLUB_DUES, payment.getFinalPrice());
        }

        @Test
        void 쿠폰_적용_시_할인된_가격이_적용된다() {
            // given
            Coupon coupon = create5000DiscountCoupon();
            IssuedCoupon issuedCoupon = createIssuedCoupon(member, coupon);
            PaymentRequest request = new PaymentRequest(List.of(issuedCoupon.getId()));

            // when
            paymentService.createPayment(request, userDetails);

            // then
            Payment payment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            BigDecimal discountedPrice = CLUB_DUES.subtract(coupon.getDiscountAmount());
            assertEquals(discountedPrice, payment.getFinalPrice());
        }

        @Test
        void 결제_금액이_0원일_시_즉시_완료한다() {
            // given
            Coupon coupon = Coupon.create("전액 쿠폰", CLUB_DUES);
            couponRepository.save(coupon);
            createIssuedCoupon(member, coupon);
            PaymentRequest request = new PaymentRequest(List.of(1L));

            // when
            paymentService.createPayment(request, userDetails);

            // then
            Payment payment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            System.out.println(payment.getFinalPrice());
            assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        }

        @Test
        @Transactional
        void 쿠폰_적용_시_결제_정보에_저장된다() {
            // given
            Coupon coupon = create5000DiscountCoupon();
            IssuedCoupon issuedCoupon = createIssuedCoupon(member, coupon);
            PaymentRequest request = new PaymentRequest(List.of(issuedCoupon.getId()));

            // when
            paymentService.createPayment(request, userDetails);

            // then
            Payment payment = paymentRepository.findById(1L).get();
            assertEquals(1, payment.getUsedCoupons().size());
        }

        @Test
        void 본인에게_발급되지_않은_쿠폰_사용_시_실패한다() {
            // given
            Coupon coupon = create5000DiscountCoupon();
            Member anotherMember = createMember();
            IssuedCoupon issuedCoupon = createIssuedCoupon(anotherMember, coupon);
            PaymentRequest request = new PaymentRequest(List.of(issuedCoupon.getId()));

            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> paymentService.createPayment(request, userDetails));
            assertEquals(ErrorCode.INVALID_ISSUED_COUPON, exception.getErrorCode());
            IssuedCoupon shouldNotBeUpdatedIssuedCoupon =
                    issuedCouponRepository.findById(issuedCoupon.getId()).get();
            assertEquals(true, shouldNotBeUpdatedIssuedCoupon.getIsValid());
        }

        @Test
        void 이미_PENDING_상태의_결제가_존재하면_실패한다() {
            // given
            PaymentRequest request = new PaymentRequest(List.of());
            paymentService.createPayment(request, userDetails);

            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> paymentService.createPayment(request, userDetails));
            assertEquals(ErrorCode.PAYMENT_ALREADY_EXISTS, exception.getErrorCode());
        }
    }

    @Nested
    class 결제정보_수정 {

        @Test
        void 성공한다() {
            // given
            PaymentRequest createRequest = new PaymentRequest(List.of());
            paymentService.createPayment(createRequest, userDetails);

            Coupon coupon = create5000DiscountCoupon();
            createIssuedCoupon(member, coupon);
            PaymentRequest updateRequest = new PaymentRequest(List.of(1L));

            // when
            paymentService.updatePayment(updateRequest, userDetails);

            // then
            Payment payment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
            assertEquals(CLUB_DUES.subtract(coupon.getDiscountAmount()), payment.getFinalPrice());
        }

        @Test
        void 쿠폰을_제거하면_할인이_취소된다() {
            // given
            Coupon coupon = create5000DiscountCoupon();
            createIssuedCoupon(member, coupon);
            PaymentRequest createRequest = new PaymentRequest(List.of(1L));
            paymentService.createPayment(createRequest, userDetails);

            PaymentRequest updateRequest = new PaymentRequest(List.of());

            // when
            paymentService.updatePayment(updateRequest, userDetails);

            // then
            Payment payment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
            assertEquals(CLUB_DUES, payment.getFinalPrice());
        }

        @Test
        void PENDING_상태의_결제가_없으면_실패한다() {
            // given
            PaymentRequest request = new PaymentRequest(List.of());

            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> paymentService.updatePayment(request, userDetails));
            assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @Transactional
        void 완료된_결제정보가_존재하면_실패한다() {
            // given
            PaymentRequest request = new PaymentRequest(List.of());

            Payment payment = Payment.of(member);
            ReflectionTestUtils.setField(payment, "status", PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> paymentService.updatePayment(request, userDetails));
            assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @Transactional
        void 초과입금된_결제정보가_존재하면_실패한다() {
            // given
            PaymentRequest request = new PaymentRequest(List.of());

            Payment payment = Payment.of(member);
            ReflectionTestUtils.setField(payment, "status", PaymentStatus.OVERPAID);
            paymentRepository.save(payment);

            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> paymentService.updatePayment(request, userDetails));
            assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
        }
    }
}
