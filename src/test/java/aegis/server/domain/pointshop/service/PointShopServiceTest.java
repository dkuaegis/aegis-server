package aegis.server.domain.pointshop.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.domain.PointTransactionType;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.domain.point.repository.PointTransactionRepository;
import aegis.server.domain.pointshop.domain.PointShopDrawHistory;
import aegis.server.domain.pointshop.dto.response.PointShopDrawHistoryResponse;
import aegis.server.domain.pointshop.dto.response.PointShopDrawResponse;
import aegis.server.domain.pointshop.repository.PointShopDrawHistoryRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

class PointShopServiceTest extends IntegrationTest {

    @Autowired
    PointShopService pointShopService;

    @Autowired
    PointAccountRepository pointAccountRepository;

    @Autowired
    PointTransactionRepository pointTransactionRepository;

    @Autowired
    PointShopDrawHistoryRepository pointShopDrawHistoryRepository;

    @Nested
    class 포인트샵_뽑기 {

        @Nested
        class 내_뽑기_기록_조회 {

            @Test
            void 기록이_없으면_빈_리스트를_반환한다() {
                // given
                Member member = createMember();
                UserDetails userDetails = createUserDetails(member);

                // when
                var responses = pointShopService.getMyDrawHistories(userDetails);

                // then: 반환값 검증 (조회 로직은 반환값만 검증)
                assertNotNull(responses);
                assertTrue(responses.isEmpty());
            }

            @Test
            void 최신순으로_조회된다() {
                // given
                Member member = createMember();
                UserDetails userDetails = createUserDetails(member);

                PointAccount account = pointAccountRepository.save(PointAccount.create(member));
                account.add(BigDecimal.valueOf(1000));

                // draw 3회
                pointShopService.draw(userDetails);
                pointShopService.draw(userDetails);
                pointShopService.draw(userDetails);

                // when
                var histories = pointShopService.getMyDrawHistories(userDetails);

                // then: 반환값 검증
                assertNotNull(histories);
                assertEquals(3, histories.size());

                PointShopDrawHistoryResponse first = histories.get(0);
                PointShopDrawHistoryResponse second = histories.get(1);
                PointShopDrawHistoryResponse third = histories.get(2);

                assertNotNull(first.drawHistoryId());
                assertNotNull(first.item());
                assertNotNull(first.transactionId());
                assertNotNull(first.createdAt());

                // 정렬: id DESC
                assertTrue(first.drawHistoryId() > second.drawHistoryId());
                assertTrue(second.drawHistoryId() > third.drawHistoryId());
            }
        }

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            PointAccount account = pointAccountRepository.save(PointAccount.create(member));
            account.add(BigDecimal.valueOf(1000));

            // when
            PointShopDrawResponse response = pointShopService.draw(userDetails);

            // then: 반환값 검증
            assertNotNull(response);
            assertNotNull(response.item());
            assertEquals(BigDecimal.valueOf(900), response.remainingBalance());
            assertNotNull(response.transactionId());
            assertNotNull(response.drawHistoryId());

            // then: DB 상태 검증
            PointAccount updated =
                    pointAccountRepository.findById(member.getId()).get();
            assertEquals(BigDecimal.valueOf(900), updated.getBalance());

            List<PointTransaction> transactions = pointTransactionRepository.findAllByPointAccountId(member.getId());
            assertFalse(transactions.isEmpty());
            PointTransaction latest = transactions.getFirst();
            assertEquals(PointTransactionType.SPEND, latest.getTransactionType());
            assertEquals(BigDecimal.valueOf(100), latest.getAmount());

            PointShopDrawHistory history = pointShopDrawHistoryRepository
                    .findById(response.drawHistoryId())
                    .orElseThrow();
            assertEquals(member.getId(), history.getMember().getId());
            assertEquals(latest.getId(), history.getPointTransaction().getId());
        }

        @Test
        void 잔액_부족으로_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            PointAccount account = pointAccountRepository.save(PointAccount.create(member));
            account.add(BigDecimal.valueOf(50));
            pointAccountRepository.save(account);

            // when-then
            CustomException ex = assertThrows(CustomException.class, () -> pointShopService.draw(userDetails));
            assertEquals(ErrorCode.POINT_INSUFFICIENT_BALANCE, ex.getErrorCode());
        }
    }
}
