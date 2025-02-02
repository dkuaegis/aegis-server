package aegis.server.domain.coupon.service;

import aegis.server.domain.coupon.domain.Coupon;
import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.coupon.dto.request.CouponCreateRequest;
import aegis.server.domain.coupon.dto.request.CouponIssueRequest;
import aegis.server.domain.coupon.repository.CouponRepository;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.helper.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CouponServiceTest extends IntegrationTest {

    @Autowired
    CouponService couponService;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    IssuedCouponRepository issuedCouponRepository;

    private static final String COUPON_NAME = "쿠폰명";

    @Nested
    class 쿠폰생성 {

        @Test
        void 성공한다() {
            // given
            CouponCreateRequest couponCreateRequest = new CouponCreateRequest(COUPON_NAME, BigDecimal.valueOf(5000));

            // when
            couponService.createCoupon(couponCreateRequest);

            // then
            Coupon coupon = couponRepository.findById(1L).get();

            assertEquals(couponCreateRequest.couponName(), coupon.getCouponName());
            assertEquals(couponCreateRequest.discountAmount(), coupon.getDiscountAmount());
        }

        @Test
        void 할인금액이_0_이하면_실패한다() {
            // given
            CouponCreateRequest request1 = new CouponCreateRequest(COUPON_NAME, BigDecimal.ZERO);
            CouponCreateRequest request2 = new CouponCreateRequest(COUPON_NAME, BigDecimal.valueOf(-5000L));

            // when-then
            CustomException exception1 = assertThrows(CustomException.class,
                    () -> couponService.createCoupon(request1));
            assertEquals(ErrorCode.COUPON_DISCOUNT_AMOUNT_NOT_POSITIVE, exception1.getErrorCode());

            CustomException exception2 = assertThrows(CustomException.class,
                    () -> couponService.createCoupon(request2));
            assertEquals(ErrorCode.COUPON_DISCOUNT_AMOUNT_NOT_POSITIVE, exception2.getErrorCode());
        }

        @Test
        void 중복된_이름은_실패한다() {
            // given
            CouponCreateRequest request = new CouponCreateRequest(COUPON_NAME, BigDecimal.valueOf(5000L));
            couponService.createCoupon(request);

            // when-then
            CustomException exception = assertThrows(CustomException.class,
                    () -> couponService.createCoupon(request));
            assertEquals(ErrorCode.COUPON_ALREADY_EXISTS, exception.getErrorCode());
        }

        @Nested
        class 쿠폰발급 {

            @Test
            void 성공한다() {
                // given
                createMember();
                createMember();

                CouponCreateRequest couponCreateRequest = new CouponCreateRequest(COUPON_NAME, BigDecimal.valueOf(5000L));
                couponService.createCoupon(couponCreateRequest);

                CouponIssueRequest couponIssueRequest = new CouponIssueRequest(1L, List.of(1L, 2L));

                // when
                couponService.createIssuedCoupon(couponIssueRequest);

                // then
                List<IssuedCoupon> issuedCoupons = issuedCouponRepository.findAll();
                assertEquals(2, issuedCoupons.size());
                assertEquals(1L, issuedCoupons.get(0).getMember().getId());
                assertEquals(2L, issuedCoupons.get(1).getMember().getId());
            }

            @Test
            void 존재하지_않는_멤버이면_제외하고_성공한다() {
                // given
                createMember();

                CouponCreateRequest couponCreateRequest = new CouponCreateRequest(COUPON_NAME, BigDecimal.valueOf(5000L));
                couponService.createCoupon(couponCreateRequest);

                CouponIssueRequest couponIssueRequest = new CouponIssueRequest(1L, List.of(1L, 2L));

                // when
                couponService.createIssuedCoupon(couponIssueRequest);

                // then
                List<IssuedCoupon> issuedCoupons = issuedCouponRepository.findAll();
                assertEquals(1, issuedCoupons.size());
                assertEquals(1L, issuedCoupons.getFirst().getMember().getId());
            }

            @Test
            void 존재하지_않는_쿠폰이면_실패한다() {
                // given
                createMember();

                CouponIssueRequest couponIssueRequest = new CouponIssueRequest(1L, List.of(1L));

                // when-then
                CustomException exception = assertThrows(CustomException.class,
                        () -> couponService.createIssuedCoupon(couponIssueRequest));
                assertEquals(ErrorCode.COUPON_NOT_FOUND, exception.getErrorCode());
            }
        }
    }
}
