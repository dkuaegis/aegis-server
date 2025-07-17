package aegis.server.domain.point.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.domain.PointTransactionType;
import aegis.server.domain.point.dto.response.PointSummaryResponse;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.domain.point.repository.PointTransactionRepository;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PointServiceTest extends IntegrationTest {

    @Autowired
    PointService pointService;

    @Autowired
    PointAccountRepository pointAccountRepository;

    @Autowired
    PointTransactionRepository pointTransactionRepository;

    @Nested
    class 포인트_요약_조회 {

        @Test
        void 기존_포인트_계정이_있다면_성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            PointAccount pointAccount = PointAccount.create(member);
            pointAccount.add(BigDecimal.valueOf(1000));
            pointAccount = pointAccountRepository.save(pointAccount);

            PointTransaction transaction1 =
                    PointTransaction.create(pointAccount, PointTransactionType.EARN, BigDecimal.valueOf(500), "테스트 적립");
            PointTransaction transaction2 = PointTransaction.create(
                    pointAccount, PointTransactionType.SPEND, BigDecimal.valueOf(200), "테스트 사용");
            pointTransactionRepository.save(transaction1);
            pointTransactionRepository.save(transaction2);

            // when
            PointSummaryResponse response = pointService.getPointSummary(userDetails);

            // then
            assertEquals(BigDecimal.valueOf(1000), response.balance());
            assertEquals(2, response.history().size());
            assertEquals(PointTransactionType.SPEND, response.history().get(0).transactionType());
            assertEquals(BigDecimal.valueOf(200), response.history().get(0).amount());
            assertEquals("테스트 사용", response.history().get(0).reason());
            assertEquals(PointTransactionType.EARN, response.history().get(1).transactionType());
            assertEquals(BigDecimal.valueOf(500), response.history().get(1).amount());
            assertEquals("테스트 적립", response.history().get(1).reason());
        }

        @Test
        void 빈_거래_내역으로_성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            PointAccount pointAccount = PointAccount.create(member);
            pointAccount.add(BigDecimal.valueOf(500));
            pointAccountRepository.save(pointAccount);

            // when
            PointSummaryResponse response = pointService.getPointSummary(userDetails);

            // then
            assertEquals(BigDecimal.valueOf(500), response.balance());
            assertEquals(0, response.history().size());
        }

        @Test
        void 여러_거래_내역이_있다면_모두_조회한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            PointAccount pointAccount = PointAccount.create(member);
            pointAccount.add(BigDecimal.valueOf(2000));
            pointAccount = pointAccountRepository.save(pointAccount);

            List<PointTransaction> transactions = List.of(
                    PointTransaction.create(
                            pointAccount, PointTransactionType.EARN, BigDecimal.valueOf(1000), "첫 번째 적립"),
                    PointTransaction.create(
                            pointAccount, PointTransactionType.EARN, BigDecimal.valueOf(1500), "두 번째 적립"),
                    PointTransaction.create(
                            pointAccount, PointTransactionType.SPEND, BigDecimal.valueOf(500), "첫 번째 사용"));
            pointTransactionRepository.saveAll(transactions);

            // when
            PointSummaryResponse response = pointService.getPointSummary(userDetails);

            // then
            assertEquals(BigDecimal.valueOf(2000), response.balance());
            assertEquals(3, response.history().size());

            // 거래 내역이 올바른 순서로 반환되는지 확인 (최신 순서대로)
            assertEquals("첫 번째 사용", response.history().get(0).reason());
            assertEquals("두 번째 적립", response.history().get(1).reason());
            assertEquals("첫 번째 적립", response.history().get(2).reason());
        }
    }
}
