package aegis.server.domain.member.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.*;
import aegis.server.domain.member.dto.response.MypageResponse;
import aegis.server.domain.point.domain.PointAccount;
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

    @Nested
    class 마이페이지_요약_조회 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            PointAccount pointAccount = createPointAccount(member);
            createEarnPointTransaction(pointAccount, BigDecimal.valueOf(1000), "테스트 적립");
            createSpendPointTransaction(pointAccount, BigDecimal.valueOf(300), "테스트 사용");

            // when
            MypageResponse response = mypageService.getMypageSummary(userDetails);

            // then
            assertNotNull(response);
            assertEquals(member.getName(), response.name());
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
            assertEquals(BigDecimal.ZERO, response.pointBalance()); // 거래 내역이 없으면 0
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

        @Test
        void 복합적인_거래_내역으로_성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            PointAccount pointAccount = createPointAccount(member);
            createEarnPointTransaction(pointAccount, BigDecimal.valueOf(2000), "첫 번째 적립");
            createSpendPointTransaction(pointAccount, BigDecimal.valueOf(500), "첫 번째 사용");
            createEarnPointTransaction(pointAccount, BigDecimal.valueOf(1500), "두 번째 적립");
            createSpendPointTransaction(pointAccount, BigDecimal.valueOf(800), "두 번째 사용");

            // when
            MypageResponse response = mypageService.getMypageSummary(userDetails);

            // then
            assertNotNull(response);
            assertEquals(member.getName(), response.name());
            assertEquals(BigDecimal.valueOf(2200), response.pointBalance()); // 2000 - 500 + 1500 - 800 = 2200
        }
    }
}
