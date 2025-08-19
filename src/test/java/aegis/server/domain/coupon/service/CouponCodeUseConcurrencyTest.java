package aegis.server.domain.coupon.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import aegis.server.domain.coupon.domain.Coupon;
import aegis.server.domain.coupon.domain.CouponCode;
import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.coupon.dto.request.CouponCodeCreateRequest;
import aegis.server.domain.coupon.dto.request.CouponCodeUseRequest;
import aegis.server.domain.coupon.dto.response.CouponCodeResponse;
import aegis.server.domain.coupon.repository.CouponCodeRepository;
import aegis.server.domain.coupon.repository.CouponRepository;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.domain.member.domain.*;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTestWithoutTransactional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("concurrency")
@ActiveProfiles("postgres")
class CouponCodeUseConcurrencyTest extends IntegrationTestWithoutTransactional {

    @Autowired
    CouponService couponService;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    CouponCodeRepository couponCodeRepository;

    @Autowired
    IssuedCouponRepository issuedCouponRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void 동시에_같은_쿠폰코드를_사용해도_한_번만_사용된다() throws InterruptedException {
        // 테스트 변수 설정
        int userCount = 1000; // 동시 사용자 수

        // given
        Coupon coupon = createCoupon();
        CouponCodeCreateRequest codeCreateRequest = new CouponCodeCreateRequest(coupon.getId());
        CouponCodeResponse codeResponse = couponService.createCouponCode(codeCreateRequest);
        String couponCode = codeResponse.code();

        // 미리 멤버들을 생성하여 동시성 문제 방지
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            members.add(createUniqueTestMember(i));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(userCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(userCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        List<CompletableFuture<Void>> futures = IntStream.range(0, userCount)
                .mapToObj(i -> CompletableFuture.runAsync(
                        () -> {
                            try {
                                startLatch.await();

                                Member member = members.get(i);
                                UserDetails userDetails = createUserDetails(member);

                                couponService.useCouponCode(userDetails, new CouponCodeUseRequest(couponCode));
                                successCount.incrementAndGet();

                            } catch (CustomException e) {
                                if (e.getErrorCode() == ErrorCode.COUPON_CODE_ALREADY_USED) {
                                    failCount.incrementAndGet();
                                } else {
                                    throw new RuntimeException("예상하지 못한 예외 발생: " + e.getErrorCode());
                                }
                            } catch (Exception e) {
                                throw new RuntimeException("예상하지 못한 예외 발생", e);
                            } finally {
                                endLatch.countDown();
                            }
                        },
                        executorService))
                .toList();

        // 모든 스레드가 동시에 시작되도록 신호 전송
        startLatch.countDown();

        // 모든 스레드가 완료될 때까지 대기
        endLatch.await();

        // CompletableFuture 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        executorService.shutdown();

        // then
        CouponCode usedCouponCode =
                couponCodeRepository.findById(codeResponse.codeCouponId()).get();
        assertFalse(usedCouponCode.getIsValid());
        assertNotNull(usedCouponCode.getIssuedCoupon());

        // 검증: 성공한 사용자는 정확히 1명
        assertEquals(1, successCount.get(), "쿠폰 코드는 정확히 1번만 사용되어야 합니다");

        // 검증: 실패한 사용자는 userCount - 1명
        assertEquals(userCount - 1, failCount.get(), "나머지 사용자들은 모두 실패해야 합니다");

        // 검증: 총 요청 수 = 성공 수 + 실패 수
        assertEquals(userCount, successCount.get() + failCount.get(), "총 요청 수와 성공/실패 수의 합이 일치하지 않습니다");

        // 검증: 발급된 쿠폰이 정확히 1개만 생성되었는지 확인
        List<IssuedCoupon> issuedCoupons = issuedCouponRepository.findAll();
        long issuedCouponsForThisCoupon = issuedCoupons.stream()
                .filter(ic -> ic.getCoupon().getId().equals(coupon.getId()))
                .count();
        assertEquals(1, issuedCouponsForThisCoupon, "해당 쿠폰에 대해 발급된 쿠폰은 정확히 1개여야 합니다");

        // 결과 출력
        System.out.printf(
                "쿠폰 코드 동시성 테스트 결과 - 동시 사용자: %d명, 성공: %d명, 실패: %d명%n", userCount, successCount.get(), failCount.get());
    }

    private Coupon createCoupon() {
        Coupon coupon = Coupon.create("동시성테스트쿠폰", BigDecimal.valueOf(5000L));
        return couponRepository.save(coupon);
    }

    private Member createUniqueTestMember(int index) {
        String uniqueId = "coupon_test_user_" + System.currentTimeMillis() + "_" + index;
        Member member = Member.create(uniqueId, uniqueId + "@dankook.ac.kr", "쿠폰테스트사용자" + index);
        member.updatePersonalInfo(
                "010-1234-567" + (index % 10),
                "32000" + String.format("%03d", index % 1000),
                Department.SW융합대학_컴퓨터공학과,
                Grade.THREE,
                "010101",
                Gender.MALE);
        member.promoteToUser();

        return memberRepository.save(member);
    }
}
