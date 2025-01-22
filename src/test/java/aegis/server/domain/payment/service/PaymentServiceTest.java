package aegis.server.domain.payment.service;

import aegis.server.common.IntegrationTest;
import aegis.server.domain.coupon.domain.Coupon;
import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.dto.request.PaymentRequest;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.global.security.dto.SessionUser;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static aegis.server.global.constant.Constant.CLUB_DUES;
import static aegis.server.global.constant.Constant.CURRENT_SEMESTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PaymentServiceTest extends IntegrationTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    IssuedCouponRepository issuedCouponRepository;

    @Nested
    class 결제정보_생성 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            SessionUser sessionUser = createSessionUser(member);
            PaymentRequest request = new PaymentRequest(List.of());

            // when
            paymentService.createOrUpdatePendingPayment(request, sessionUser);

            // then
            Payment payment = paymentRepository.findByMemberAndCurrentSemester(member, CURRENT_SEMESTER).orElseThrow();
            assertEquals(member.getId(), payment.getMember().getId());
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
            assertEquals(CLUB_DUES, payment.getOriginalPrice());
        }

        @Test
        void 쿠폰_사용_시_할인된_가격이_적용된다() {
            // given
            Member member = createMember();
            SessionUser sessionUser = createSessionUser(member);

            Coupon coupon = createCoupon();
            IssuedCoupon issuedCoupon = createIssuedCoupon(member, coupon);
            PaymentRequest request = new PaymentRequest(List.of(issuedCoupon.getId()));

            // when
            paymentService.createOrUpdatePendingPayment(request, sessionUser);

            // then
            Payment payment = paymentRepository.findByMemberAndCurrentSemester(member, CURRENT_SEMESTER).orElseThrow();
            BigDecimal discountedPrice = CLUB_DUES.subtract(coupon.getDiscountAmount());
            assertEquals(discountedPrice, payment.getFinalPrice());
        }

        @Test
        void 쿠폰_사용_시_사용처리_된다() {
            // given
            Member member = createMember();
            SessionUser sessionUser = createSessionUser(member);

            Coupon coupon = createCoupon();
            IssuedCoupon issuedCoupon = createIssuedCoupon(member, coupon);
            PaymentRequest request = new PaymentRequest(List.of(issuedCoupon.getId()));

            // when
            paymentService.createOrUpdatePendingPayment(request, sessionUser);

            // then
            IssuedCoupon updatedIssuedCoupon = issuedCouponRepository.findById(issuedCoupon.getId()).orElseThrow();
            assertEquals(false, updatedIssuedCoupon.getIsValid());
        }

        @Test
        void 중복된_결제정보_생성_시_기존_정보를_덮어씌운다() {
            // given
            Member member = createMember();
            SessionUser sessionUser = createSessionUser(member);
            PaymentRequest oldRequest = new PaymentRequest(List.of());

            paymentService.createOrUpdatePendingPayment(oldRequest, sessionUser);
            Payment firstPayment = paymentRepository.findById(1L).orElseThrow();

            // when
            Coupon coupon = createCoupon();
            createIssuedCoupon(member, coupon);
            PaymentRequest newRequest = new PaymentRequest(List.of(1L));
            paymentService.createOrUpdatePendingPayment(newRequest, sessionUser);
            Payment secondPayment = paymentRepository.findById(1L).orElseThrow();

            // then
            assertEquals(PaymentStatus.PENDING, secondPayment.getStatus());
            assertEquals(CLUB_DUES.subtract(coupon.getDiscountAmount()), secondPayment.getFinalPrice());
        }

        @Test
        void 완료된_결제정보가_존재하면_실패한다() {
            // given
            Member member = createMember();
            SessionUser sessionUser = createSessionUser(member);
            PaymentRequest request = new PaymentRequest(List.of());

            Payment payment = Payment.of(member);
            ReflectionTestUtils.setField(payment, "status", PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            // when
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                paymentService.createOrUpdatePendingPayment(request, sessionUser);
            });

            // then
            assertEquals("완료된 결제 정보가 존재합니다.", exception.getMessage());
        }

        @Test
        void 초과입금된_결제정보가_존재하면_실패한다() {
            // given
            Member member = createMember();
            SessionUser sessionUser = createSessionUser(member);
            PaymentRequest request = new PaymentRequest(List.of());

            Payment payment = Payment.of(member);
            ReflectionTestUtils.setField(payment, "status", PaymentStatus.OVERPAID);
            paymentRepository.save(payment);

            // when
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                paymentService.createOrUpdatePendingPayment(request, sessionUser);
            });

            // then
            assertEquals("초과입금된 결제 정보가 존재합니다.", exception.getMessage());
        }
    }
}
