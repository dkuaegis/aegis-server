package aegis.server.domain.member.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.dto.response.MypageResponse;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MypageServiceTest extends IntegrationTest {

    @Autowired
    MypageService mypageService;

    @Autowired
    PointAccountRepository pointAccountRepository;

    @Nested
    class 마이페이지_요약_조회 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            PointAccount pointAccount = createPointAccount(member);
            createEarnTransaction(pointAccount, BigDecimal.valueOf(1000), "테스트 적립");
            createSpendTransaction(pointAccount, BigDecimal.valueOf(300), "테스트 사용");

            // when
            MypageResponse response = mypageService.getMypageSummary(userDetails);

            // then
            assertNotNull(response);
            assertEquals(member.getName(), response.name());
            assertEquals(member.getProfileIcon(), response.profileIcon());
            assertEquals(BigDecimal.valueOf(700), response.pointBalance()); // 1000 - 300 = 700
        }

        @Test
        void 포인트_거래_내역이_없어도_성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            createPointAccount(member);

            // when
            MypageResponse response = mypageService.getMypageSummary(userDetails);

            // then
            assertNotNull(response);
            assertEquals(member.getName(), response.name());
            assertEquals(member.getProfileIcon(), response.profileIcon());
            assertEquals(BigDecimal.ZERO, response.pointBalance()); // 거래 내역이 없으면 0
        }

        @Test
        void 초기_회원정보만_있어도_성공한다() {
            // given
            Member member = createInitialMember(); // 개인정보가 완성되지 않은 초기 회원
            UserDetails userDetails = createUserDetails(member);
            createPointAccount(member);

            // when
            MypageResponse response = mypageService.getMypageSummary(userDetails);

            // then
            assertNotNull(response);
            assertEquals(member.getName(), response.name());
            assertEquals(member.getProfileIcon(), response.profileIcon());
            assertEquals(BigDecimal.ZERO, response.pointBalance());
        }

        @Test
        void member를_찾을_수_없다면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            ReflectionTestUtils.setField(userDetails, "memberId", member.getId() + 1L);

            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> mypageService.getMypageSummary(userDetails));
            assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
        }
    }
}
