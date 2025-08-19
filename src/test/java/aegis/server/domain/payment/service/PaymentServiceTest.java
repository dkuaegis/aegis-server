package aegis.server.domain.payment.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.coupon.domain.Coupon;
import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.coupon.repository.CouponRepository;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.dto.request.PaymentRequest;
import aegis.server.domain.payment.dto.response.PaymentResponse;
import aegis.server.domain.payment.dto.response.PaymentStatusResponse;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTestWithoutTransactional;

import static aegis.server.global.constant.Constant.CLUB_DUES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PaymentServiceTest extends IntegrationTestWithoutTransactional {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    IssuedCouponRepository issuedCouponRepository;

    @Autowired
    CouponRepository couponRepository;

    @Nested
    class 결제정보_생성 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            PaymentRequest request = new PaymentRequest(List.of());

            // when
            PaymentResponse response = paymentService.createPayment(request, userDetails);

            // then
            // 반환값 검증
            assertEquals(PaymentStatus.PENDING, response.status());
            assertEquals(CLUB_DUES, response.finalPrice());

            // DB 상태 검증
            Payment payment = paymentRepository.findById(response.id()).get();
            assertEquals(member.getId(), payment.getMember().getId());
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
            assertEquals(CLUB_DUES, payment.getFinalPrice());
        }

        @Test
        void 쿠폰_적용_시_할인된_가격이_적용된다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Coupon coupon = createCoupon();
            IssuedCoupon issuedCoupon = createIssuedCoupon(member, coupon);
            PaymentRequest request = new PaymentRequest(List.of(issuedCoupon.getId()));

            // when
            PaymentResponse response = paymentService.createPayment(request, userDetails);

            // then
            // 반환값 검증
            BigDecimal discountedPrice = CLUB_DUES.subtract(coupon.getDiscountAmount());
            assertEquals(discountedPrice, response.finalPrice());
            assertEquals(PaymentStatus.PENDING, response.status());

            // DB 상태 검증
            Payment payment = paymentRepository.findById(response.id()).get();
            assertEquals(discountedPrice, payment.getFinalPrice());
        }

        @Test
        void 결제_금액이_0원일_시_즉시_완료한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Coupon coupon = Coupon.create("전액 쿠폰", CLUB_DUES);
            couponRepository.save(coupon);
            IssuedCoupon issuedCoupon = createIssuedCoupon(member, coupon);
            PaymentRequest request = new PaymentRequest(List.of(issuedCoupon.getId()));

            // when
            PaymentResponse response = paymentService.createPayment(request, userDetails);

            // then
            // 반환값 검증
            assertEquals(PaymentStatus.COMPLETED, response.status());
            assertEquals(BigDecimal.ZERO, response.finalPrice());

            // DB 상태 검증
            Payment payment = paymentRepository.findById(response.id()).get();
            assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
            assertEquals(BigDecimal.ZERO, payment.getFinalPrice());
        }

        @Test
        void 할인_금액이_원래_가격보다_클_때_최종가격이_0원이_된다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Coupon coupon1 = Coupon.create("대형 할인 쿠폰1", CLUB_DUES);
            Coupon coupon2 = Coupon.create("대형 할인 쿠폰2", BigDecimal.valueOf(10000));
            couponRepository.save(coupon1);
            couponRepository.save(coupon2);

            IssuedCoupon issuedCoupon1 = createIssuedCoupon(member, coupon1);
            IssuedCoupon issuedCoupon2 = createIssuedCoupon(member, coupon2);

            PaymentRequest request = new PaymentRequest(List.of(issuedCoupon1.getId(), issuedCoupon2.getId()));

            // when
            paymentService.createPayment(request, userDetails);

            // then
            Payment payment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            assertEquals(BigDecimal.ZERO, payment.getFinalPrice());
            assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        }

        @Test
        @Transactional
        void 쿠폰_적용_시_결제_정보에_저장된다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Coupon coupon = createCoupon();
            IssuedCoupon issuedCoupon = createIssuedCoupon(member, coupon);
            PaymentRequest request = new PaymentRequest(List.of(issuedCoupon.getId()));

            // when
            PaymentResponse response = paymentService.createPayment(request, userDetails);

            // then
            Payment payment = paymentRepository.findById(response.id()).get();
            assertEquals(1, payment.getUsedCoupons().size());
        }

        @Test
        void 본인에게_발급되지_않은_쿠폰_사용_시_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Coupon coupon = createCoupon();
            Member anotherMember = createMember();
            IssuedCoupon issuedCoupon = createIssuedCoupon(anotherMember, coupon);
            PaymentRequest request = new PaymentRequest(List.of(issuedCoupon.getId()));

            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> paymentService.createPayment(request, userDetails));
            assertEquals(ErrorCode.INVALID_ISSUED_COUPON_INCLUDED, exception.getErrorCode());

            IssuedCoupon shouldNotBeUpdatedIssuedCoupon =
                    issuedCouponRepository.findById(issuedCoupon.getId()).get();
            assertEquals(true, shouldNotBeUpdatedIssuedCoupon.getIsValid());
        }

        @Test
        void 이미_PENDING_상태의_결제가_존재하면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            PaymentRequest request = new PaymentRequest(List.of());
            paymentService.createPayment(request, userDetails);

            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> paymentService.createPayment(request, userDetails));
            assertEquals(ErrorCode.PAYMENT_ALREADY_EXISTS, exception.getErrorCode());
        }

        @Test
        void 이전_학기_결제_완료_후_새_학기_결제가_성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            PaymentRequest request1 = new PaymentRequest(List.of());
            PaymentResponse firstPayment = paymentService.createPayment(request1, userDetails);

            // 완료된 결제 상태로 변경 후 새로운 학기로 설정
            Payment payment = paymentRepository.findById(firstPayment.id()).get();
            ReflectionTestUtils.setField(payment, "status", PaymentStatus.COMPLETED);
            ReflectionTestUtils.setField(payment, "yearSemester", YearSemester.YEAR_SEMESTER_2025_1);
            paymentRepository.save(payment);

            PaymentRequest request2 = new PaymentRequest(List.of());

            // when
            paymentService.createPayment(request2, userDetails);

            // then
            Payment newPayment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            assertEquals(YearSemester.YEAR_SEMESTER_2025_2, newPayment.getYearSemester());
            assertEquals(PaymentStatus.PENDING, newPayment.getStatus());
        }

        @Test
        void 이전_학기_결제_미완료_상태에서도_새_학기_결제가_성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            PaymentRequest request1 = new PaymentRequest(List.of());
            PaymentResponse firstPayment = paymentService.createPayment(request1, userDetails);

            // 이전 학기로 변경
            Payment oldPayment = paymentRepository.findById(firstPayment.id()).get();
            ReflectionTestUtils.setField(oldPayment, "yearSemester", YearSemester.YEAR_SEMESTER_2025_1);
            paymentRepository.save(oldPayment);

            PaymentRequest request2 = new PaymentRequest(List.of());

            // when
            paymentService.createPayment(request2, userDetails);

            // then
            Payment newPayment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            assertEquals(YearSemester.YEAR_SEMESTER_2025_2, newPayment.getYearSemester());
            assertEquals(PaymentStatus.PENDING, newPayment.getStatus());
        }
    }

    @Nested
    class 결제정보_수정 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            PaymentRequest createRequest = new PaymentRequest(List.of());
            paymentService.createPayment(createRequest, userDetails);

            Coupon coupon = createCoupon();
            IssuedCoupon issuedCoupon = createIssuedCoupon(member, coupon);
            PaymentRequest updateRequest = new PaymentRequest(List.of(issuedCoupon.getId()));

            // when
            PaymentResponse response = paymentService.updatePayment(updateRequest, userDetails);

            // then
            // 반환값 검증
            assertEquals(PaymentStatus.PENDING, response.status());
            assertEquals(CLUB_DUES.subtract(coupon.getDiscountAmount()), response.finalPrice());

            // DB 상태 검증
            Payment payment = paymentRepository.findById(response.id()).get();
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
            assertEquals(CLUB_DUES.subtract(coupon.getDiscountAmount()), payment.getFinalPrice());
        }

        @Test
        void 쿠폰을_제거하면_할인이_취소된다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Coupon coupon = createCoupon();
            IssuedCoupon issuedCoupon = createIssuedCoupon(member, coupon);
            PaymentRequest createRequest = new PaymentRequest(List.of(issuedCoupon.getId()));
            paymentService.createPayment(createRequest, userDetails);

            PaymentRequest updateRequest = new PaymentRequest(List.of());

            // when
            PaymentResponse response = paymentService.updatePayment(updateRequest, userDetails);

            // then
            // 반환값 검증
            assertEquals(PaymentStatus.PENDING, response.status());
            assertEquals(CLUB_DUES, response.finalPrice());

            // DB 상태 검증
            Payment payment = paymentRepository.findById(response.id()).get();
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
            assertEquals(CLUB_DUES, payment.getFinalPrice());
        }

        @Test
        void PENDING_상태의_결제가_없으면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            PaymentRequest request = new PaymentRequest(List.of());

            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> paymentService.updatePayment(request, userDetails));
            assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 쿠폰_적용으로_0원이_되면_즉시_완료한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            PaymentRequest createRequest = new PaymentRequest(List.of());
            paymentService.createPayment(createRequest, userDetails);

            Coupon coupon = Coupon.create("전액 쿠폰", CLUB_DUES);
            couponRepository.save(coupon);
            IssuedCoupon issuedCoupon = createIssuedCoupon(member, coupon);
            PaymentRequest updateRequest = new PaymentRequest(List.of(issuedCoupon.getId()));

            // when
            PaymentResponse response = paymentService.updatePayment(updateRequest, userDetails);

            // then
            // 반환값 검증
            assertEquals(PaymentStatus.COMPLETED, response.status());
            assertEquals(BigDecimal.ZERO, response.finalPrice());

            // DB 상태 검증
            Payment payment = paymentRepository.findById(response.id()).get();
            assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
            assertEquals(BigDecimal.ZERO, payment.getFinalPrice());
        }

        @Test
        void 할인_금액이_원래_가격보다_클_때_수정_시에도_최종가격이_0원이_된다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            PaymentRequest createRequest = new PaymentRequest(List.of());
            paymentService.createPayment(createRequest, userDetails);

            Coupon coupon1 = Coupon.create("대형 할인 쿠폰1", CLUB_DUES);
            Coupon coupon2 = Coupon.create("대형 할인 쿠폰2", BigDecimal.valueOf(10000));
            couponRepository.save(coupon1);
            couponRepository.save(coupon2);

            IssuedCoupon issuedCoupon1 = createIssuedCoupon(member, coupon1);
            IssuedCoupon issuedCoupon2 = createIssuedCoupon(member, coupon2);

            PaymentRequest updateRequest = new PaymentRequest(List.of(issuedCoupon1.getId(), issuedCoupon2.getId()));

            // when
            paymentService.updatePayment(updateRequest, userDetails);

            // then
            Payment payment =
                    paymentRepository.findByMemberInCurrentYearSemester(member).get();
            assertEquals(BigDecimal.ZERO, payment.getFinalPrice());
            assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        }

        @Test
        void 완료된_결제정보가_존재하면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            PaymentRequest request = new PaymentRequest(List.of());

            Payment payment = Payment.of(member);
            ReflectionTestUtils.setField(payment, "status", PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> paymentService.updatePayment(request, userDetails));
            assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class 결제상태_조회 {

        @Test
        void PENDING_상태의_결제를_성공적으로_조회한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            PaymentRequest request = new PaymentRequest(List.of());
            paymentService.createPayment(request, userDetails);

            // when
            PaymentStatusResponse response = paymentService.checkPaymentStatus(userDetails);

            // then
            assertEquals(PaymentStatus.PENDING, response.status());
            assertEquals(CLUB_DUES, response.finalPrice());
        }

        @Test
        void COMPLETED_상태의_결제를_성공적으로_조회한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Coupon coupon = Coupon.create("전액 쿠폰", CLUB_DUES);
            couponRepository.save(coupon);
            IssuedCoupon issuedCoupon = createIssuedCoupon(member, coupon);
            PaymentRequest request = new PaymentRequest(List.of(issuedCoupon.getId()));
            paymentService.createPayment(request, userDetails);

            // when
            PaymentStatusResponse response = paymentService.checkPaymentStatus(userDetails);

            // then
            assertEquals(PaymentStatus.COMPLETED, response.status());
            assertEquals(BigDecimal.ZERO, response.finalPrice());
        }

        @Test
        void 쿠폰_할인이_적용된_결제상태를_성공적으로_조회한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Coupon coupon = createCoupon();
            IssuedCoupon issuedCoupon = createIssuedCoupon(member, coupon);
            PaymentRequest request = new PaymentRequest(List.of(issuedCoupon.getId()));
            paymentService.createPayment(request, userDetails);

            // when
            PaymentStatusResponse response = paymentService.checkPaymentStatus(userDetails);

            // then
            assertEquals(PaymentStatus.PENDING, response.status());
            assertEquals(CLUB_DUES.subtract(coupon.getDiscountAmount()), response.finalPrice());
        }

        @Test
        void 결제정보가_존재하지_않으면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            // 결제정보를 생성하지 않음

            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> paymentService.checkPaymentStatus(userDetails));
            assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
        }
    }

    private Coupon createCoupon() {
        Coupon coupon = Coupon.create("테스트쿠폰", BigDecimal.valueOf(5000L));
        return couponRepository.save(coupon);
    }

    private IssuedCoupon createIssuedCoupon(Member member, Coupon coupon) {
        return issuedCouponRepository.save(IssuedCoupon.of(coupon, member));
    }
}
