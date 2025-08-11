package aegis.server.domain.coupon.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.coupon.domain.Coupon;
import aegis.server.domain.coupon.domain.CouponCode;
import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.coupon.dto.request.CouponCodeCreateRequest;
import aegis.server.domain.coupon.dto.request.CouponCodeUseRequest;
import aegis.server.domain.coupon.dto.request.CouponCreateRequest;
import aegis.server.domain.coupon.dto.request.CouponIssueRequest;
import aegis.server.domain.coupon.dto.response.CouponCodeResponse;
import aegis.server.domain.coupon.dto.response.CouponResponse;
import aegis.server.domain.coupon.dto.response.IssuedCouponResponse;
import aegis.server.domain.coupon.repository.CouponCodeRepository;
import aegis.server.domain.coupon.repository.CouponRepository;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.domain.member.domain.*;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

class CouponServiceTest extends IntegrationTest {

    @Autowired
    CouponService couponService;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    IssuedCouponRepository issuedCouponRepository;

    @Autowired
    CouponCodeRepository couponCodeRepository;

    private static final String COUPON_NAME = "쿠폰명";

    @Nested
    class 쿠폰_조회 {
        @Test
        void 성공한다() {
            // given
            Coupon coupon1 = createCoupon("쿠폰명1");
            Coupon coupon2 = createCoupon("쿠폰명2");

            // when
            List<CouponResponse> responses = couponService.findAllCoupons();

            // then
            assertEquals(2, responses.size());
            assertTrue(
                    responses.stream().anyMatch(response -> response.couponId().equals(coupon1.getId())));
            assertTrue(
                    responses.stream().anyMatch(response -> response.couponId().equals(coupon2.getId())));
        }

        @Test
        void 쿠폰이_없는_경우_빈_리스트를_반환한다() {
            // when
            List<CouponResponse> responses = couponService.findAllCoupons();

            // then
            assertEquals(0, responses.size());
        }
    }

    @Nested
    class 발급된_쿠폰_조회 {
        @Test
        void 성공한다() {
            // given
            Member member1 = createMember();
            Member member2 = createMember();
            Coupon coupon = createCoupon();
            createIssuedCoupon(coupon, member1);
            createIssuedCoupon(coupon, member2);

            // when
            List<IssuedCouponResponse> responses = couponService.findAllIssuedCoupons();

            // then
            assertEquals(2, responses.size());
        }

        @Test
        void 발급된_쿠폰이_없는_경우_빈_리스트를_반환한다() {
            // when
            List<IssuedCouponResponse> responses = couponService.findAllIssuedCoupons();

            // then
            assertEquals(0, responses.size());
        }
    }

    @Nested
    class 자신에게_발급된_쿠폰_조회 {
        @Test
        void 성공한다() {
            // given
            Member member1 = createMember();
            Member member2 = createMember();
            UserDetails userDetails1 = createUserDetails(member1);
            Coupon coupon = createCoupon();
            createIssuedCoupon(coupon, member1);
            createIssuedCoupon(coupon, member2);

            // when
            List<IssuedCouponResponse> responses = couponService.findMyAllIssuedCoupons(userDetails1);

            // then
            assertEquals(1, responses.size());
            assertEquals(member1.getId(), responses.getFirst().memberId());
        }

        @Test
        void 발급된_쿠폰이_없는_경우_빈_리스트를_반환한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            // when
            List<IssuedCouponResponse> responses = couponService.findMyAllIssuedCoupons(userDetails);

            // then
            assertEquals(0, responses.size());
        }
    }

    @Nested
    class 쿠폰코드_조회 {
        @Test
        void 성공한다() {
            // given
            Coupon coupon = createCoupon();
            createCouponCode(coupon);
            createCouponCode(coupon);

            // when
            List<CouponCodeResponse> responses = couponService.findAllCouponCode();

            // then
            assertEquals(2, responses.size());
        }

        @Test
        void 쿠폰코드가_없는_경우_빈_리스트를_반환한다() {
            // when
            List<CouponCodeResponse> responses = couponService.findAllCouponCode();

            // then
            assertEquals(0, responses.size());
        }
    }

    @Nested
    class 쿠폰코드_삭제 {
        @Test
        void 쿠폰코드_삭제_성공한다() {
            // given
            Coupon coupon = createCoupon();
            CouponCode couponCode = createCouponCode(coupon);

            // when
            couponService.deleteCodeCoupon(couponCode.getId());

            // then
            assertTrue(couponCodeRepository.findById(couponCode.getId()).isEmpty());
        }

        @Test
        void 존재하지_않는_쿠폰코드_삭제는_실패한다() {
            // when-then
            CustomException exception = assertThrows(CustomException.class, () -> couponService.deleteCodeCoupon(999L));
            assertEquals(ErrorCode.COUPON_CODE_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class 쿠폰생성 {
        @Test
        void 성공한다() {
            // given
            CouponCreateRequest couponCreateRequest = new CouponCreateRequest(COUPON_NAME, BigDecimal.valueOf(5000));

            // when
            CouponResponse response = couponService.createCoupon(couponCreateRequest);

            // then
            // 반환값 검증
            assertNotNull(response.couponId());
            assertNotNull(response.createdAt());
            assertEquals(couponCreateRequest.couponName(), response.couponName());
            assertEquals(couponCreateRequest.discountAmount(), response.discountAmount());

            // DB 상태 검증
            Coupon coupon = couponRepository.findById(response.couponId()).get();
            assertEquals(couponCreateRequest.couponName(), coupon.getCouponName());
            assertEquals(couponCreateRequest.discountAmount(), coupon.getDiscountAmount());
        }

        @Test
        void 할인금액이_0_이하면_실패한다() {
            // given
            CouponCreateRequest request1 = new CouponCreateRequest(COUPON_NAME, BigDecimal.ZERO);
            CouponCreateRequest request2 = new CouponCreateRequest(COUPON_NAME, BigDecimal.valueOf(-5000L));

            // when-then
            CustomException exception1 =
                    assertThrows(CustomException.class, () -> couponService.createCoupon(request1));
            assertEquals(ErrorCode.COUPON_DISCOUNT_AMOUNT_NOT_POSITIVE, exception1.getErrorCode());

            CustomException exception2 =
                    assertThrows(CustomException.class, () -> couponService.createCoupon(request2));
            assertEquals(ErrorCode.COUPON_DISCOUNT_AMOUNT_NOT_POSITIVE, exception2.getErrorCode());
        }

        @Test
        void 중복된_이름은_실패한다() {
            // given
            CouponCreateRequest request = new CouponCreateRequest(COUPON_NAME, BigDecimal.valueOf(5000L));
            couponService.createCoupon(request);

            // when-then
            CustomException exception = assertThrows(CustomException.class, () -> couponService.createCoupon(request));
            assertEquals(ErrorCode.COUPON_ALREADY_EXISTS, exception.getErrorCode());
        }
    }

    @Nested
    class 쿠폰발급 {
        @Test
        void 성공한다() {
            // given
            Member member1 = createMember();
            Member member2 = createMember();
            Coupon coupon = createCoupon();
            CouponIssueRequest couponIssueRequest =
                    new CouponIssueRequest(coupon.getId(), List.of(member1.getId(), member2.getId()));

            // when
            List<IssuedCouponResponse> responses = couponService.createIssuedCoupon(couponIssueRequest);

            // then
            // 반환값 검증
            assertEquals(2, responses.size());
            assertEquals(member1.getId(), responses.get(0).memberId());
            assertEquals(member2.getId(), responses.get(1).memberId());
            assertEquals(coupon.getCouponName(), responses.get(0).couponName());
            assertEquals(coupon.getCouponName(), responses.get(1).couponName());

            // DB 상태 검증
            IssuedCoupon issuedCoupon1 = issuedCouponRepository
                    .findById(responses.get(0).issuedCouponId())
                    .get();
            IssuedCoupon issuedCoupon2 = issuedCouponRepository
                    .findById(responses.get(1).issuedCouponId())
                    .get();
            assertEquals(member1.getId(), issuedCoupon1.getMember().getId());
            assertEquals(member2.getId(), issuedCoupon2.getMember().getId());
            assertEquals(coupon.getId(), issuedCoupon1.getCoupon().getId());
            assertEquals(coupon.getId(), issuedCoupon2.getCoupon().getId());
        }

        @Test
        void 존재하지_않는_멤버이면_제외하고_성공한다() {
            // given
            Member member = createMember();
            Coupon coupon = createCoupon();
            Long nonExistentMemberId = member.getId() + 1L;
            CouponIssueRequest couponIssueRequest =
                    new CouponIssueRequest(coupon.getId(), List.of(member.getId(), nonExistentMemberId));

            // when
            List<IssuedCouponResponse> responses = couponService.createIssuedCoupon(couponIssueRequest);

            // then
            // 반환값 검증
            assertEquals(1, responses.size());
            assertEquals(member.getId(), responses.getFirst().memberId());

            // DB 상태 검증
            IssuedCoupon issuedCoupon = issuedCouponRepository
                    .findById(responses.getFirst().issuedCouponId())
                    .get();
            assertEquals(member.getId(), issuedCoupon.getMember().getId());
            assertEquals(coupon.getId(), issuedCoupon.getCoupon().getId());
        }

        @Test
        void 존재하지_않는_쿠폰이면_실패한다() {
            // given
            Member member = createMember();
            Long nonExistentCouponId = member.getId() + 999L;
            CouponIssueRequest couponIssueRequest =
                    new CouponIssueRequest(nonExistentCouponId, List.of(member.getId()));

            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> couponService.createIssuedCoupon(couponIssueRequest));
            assertEquals(ErrorCode.COUPON_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class 쿠폰삭제 {
        @Test
        void 성공한다() {
            // given
            Coupon coupon = createCoupon();

            // when
            couponService.deleteCoupon(coupon.getId());

            // then
            assertTrue(couponRepository.findById(coupon.getId()).isEmpty());
        }

        @Test
        void 존재하지_않는_쿠폰이면_실패한다() {
            // when-then
            CustomException exception = assertThrows(CustomException.class, () -> couponService.deleteCoupon(999L));
            assertEquals(ErrorCode.COUPON_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 발급된_쿠폰이_존재하면_삭제에_실패한다() {
            // given
            Member member = createMember();
            Coupon coupon = createCoupon();
            CouponIssueRequest issueRequest = new CouponIssueRequest(coupon.getId(), List.of(member.getId()));

            // 쿠폰 발급
            couponService.createIssuedCoupon(issueRequest);

            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> couponService.deleteCoupon(coupon.getId()));
            assertEquals(ErrorCode.COUPON_ISSUED_COUPON_EXISTS, exception.getErrorCode());
        }
    }

    @Nested
    class 발급된_쿠폰삭제 {
        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            Coupon coupon = createCoupon();
            CouponIssueRequest issueRequest = new CouponIssueRequest(coupon.getId(), List.of(member.getId()));
            List<IssuedCouponResponse> responses = couponService.createIssuedCoupon(issueRequest);
            Long issuedCouponId = responses.getFirst().issuedCouponId();

            // when
            couponService.deleteIssuedCoupon(issuedCouponId);

            // then
            assertTrue(issuedCouponRepository.findById(issuedCouponId).isEmpty());
        }

        @Test
        void 존재하지_않는_발급된_쿠폰이면_실패한다() {
            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> couponService.deleteIssuedCoupon(999L));
            assertEquals(ErrorCode.ISSUED_COUPON_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class 쿠폰코드_발급 {
        @Test
        void 성공한다() {
            // given
            Coupon coupon = createCoupon();
            CouponCodeCreateRequest codeCreateRequest = new CouponCodeCreateRequest(coupon.getId());

            // when
            CouponCodeResponse response = couponService.createCouponCode(codeCreateRequest);

            // then
            // 반환값 검증
            assertEquals(coupon.getId(), response.couponId());
            assertEquals(coupon.getCouponName(), response.couponName());
            assertNotNull(response.code());
            assertTrue(response.isValid());
            assertNotNull(response.codeCouponId());

            // DB 상태 검증
            CouponCode couponCode =
                    couponCodeRepository.findById(response.codeCouponId()).get();
            assertEquals(coupon.getId(), couponCode.getCoupon().getId());
            assertTrue(couponCode.getIsValid());
            assertNotNull(couponCode.getCode());
        }
    }

    @Nested
    class 쿠폰코드_사용 {
        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Coupon coupon = createCoupon();

            CouponCodeCreateRequest codeCreateRequest = new CouponCodeCreateRequest(coupon.getId());
            CouponCodeResponse codeResponse = couponService.createCouponCode(codeCreateRequest);

            // when
            couponService.useCouponCode(userDetails, new CouponCodeUseRequest(codeResponse.code()));

            // then
            CouponCode updatedCouponCode =
                    couponCodeRepository.findById(codeResponse.codeCouponId()).get();
            assertEquals(false, updatedCouponCode.getIsValid());
            assertNotNull(updatedCouponCode.getIssuedCoupon());

            // 발급된 쿠폰이 올바르게 생성되었는지 확인
            assertEquals(
                    member.getId(),
                    updatedCouponCode.getIssuedCoupon().getMember().getId());
        }

        @Test
        void 존재하지_않는_쿠폰코드이면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            // when-then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> couponService.useCouponCode(userDetails, new CouponCodeUseRequest("존재하지않는쿠폰코드")));
            assertEquals(ErrorCode.COUPON_CODE_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 이미_사용된_쿠폰코드이면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Coupon coupon = createCoupon();

            CouponCodeCreateRequest codeCreateRequest = new CouponCodeCreateRequest(coupon.getId());
            CouponCodeResponse codeResponse = couponService.createCouponCode(codeCreateRequest);

            couponService.useCouponCode(userDetails, new CouponCodeUseRequest(codeResponse.code()));

            // when-then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> couponService.useCouponCode(userDetails, new CouponCodeUseRequest(codeResponse.code())));
            assertEquals(ErrorCode.COUPON_CODE_ALREADY_USED, exception.getErrorCode());
        }
    }

    private Coupon createCoupon() {
        Coupon coupon = Coupon.create("테스트쿠폰", BigDecimal.valueOf(5000L));
        return couponRepository.save(coupon);
    }

    private Coupon createCoupon(String name) {
        Coupon coupon = Coupon.create(name, BigDecimal.valueOf(5000L));
        return couponRepository.save(coupon);
    }

    private IssuedCoupon createIssuedCoupon(Coupon coupon, Member member) {
        IssuedCoupon issuedCoupon = IssuedCoupon.of(coupon, member);
        return issuedCouponRepository.save(issuedCoupon);
    }

    private CouponCode createCouponCode(Coupon coupon) {
        String code = CodeGenerator.generateCouponCode(8);
        CouponCode couponCode = CouponCode.of(coupon, code);
        return couponCodeRepository.save(couponCode);
    }
}
