package aegis.server.domain.point.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.*;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.domain.PointTransactionType;
import aegis.server.domain.point.dto.response.PointRankingListResponse;
import aegis.server.domain.point.dto.response.PointSummaryResponse;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PointServiceTest extends IntegrationTest {

    @Autowired
    PointService pointService;

    @Autowired
    PointAccountRepository pointAccountRepository;

    @Autowired
    PaymentRepository paymentRepository;

    private void createCompletedPaymentForCurrentSemester(Member member) {
        Payment payment = Payment.of(member);
        payment.completePayment();
        paymentRepository.save(payment);
    }

    @Nested
    class 포인트_요약_조회 {

        @Test
        void 기존_포인트_계정이_있다면_성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            PointAccount pointAccount = PointAccount.create(member);
            pointAccount = pointAccountRepository.save(pointAccount);

            createEarnPointTransaction(pointAccount, BigDecimal.valueOf(500), "테스트 적립");
            createSpendPointTransaction(pointAccount, BigDecimal.valueOf(200), "테스트 사용");

            // when
            PointSummaryResponse response = pointService.getPointSummary(userDetails);

            // then
            assertEquals(BigDecimal.valueOf(300), response.balance()); // 500 - 200 = 300
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
            pointAccount = pointAccountRepository.save(pointAccount);

            createEarnPointTransaction(pointAccount, BigDecimal.valueOf(1000), "첫 번째 적립");
            createEarnPointTransaction(pointAccount, BigDecimal.valueOf(1500), "두 번째 적립");
            createSpendPointTransaction(pointAccount, BigDecimal.valueOf(500), "첫 번째 사용");

            // when
            PointSummaryResponse response = pointService.getPointSummary(userDetails);

            // then
            assertEquals(BigDecimal.valueOf(2000), response.balance()); // 1000 + 1500 - 500 = 2000
            assertEquals(3, response.history().size());

            // 거래 내역이 올바른 순서로 반환되는지 확인 (최신 순서대로)
            assertEquals("첫 번째 사용", response.history().get(0).reason());
            assertEquals("두 번째 적립", response.history().get(1).reason());
            assertEquals("첫 번째 적립", response.history().get(2).reason());
        }
    }

    @Nested
    class 포인트_랭킹_조회 {

        @Test
        void 기본_랭킹_테스트_13명_환경() {
            // given: 13명 생성
            Member[] members = new Member[13];
            PointAccount[] accounts = new PointAccount[13];

            for (int i = 0; i < 13; i++) {
                members[i] = createMember();
                createCompletedPaymentForCurrentSemester(members[i]);
                accounts[i] = createPointAccount(members[i]);
                // 점수: 1300, 1200, 1100, ..., 200, 100 (13명)
                createEarnPointTransaction(accounts[i], BigDecimal.valueOf(1300 - (i * 100)), "적립");
            }

            Member currentUser = members[12]; // 13번째 (100포인트)
            UserDetails userDetails = createUserDetails(currentUser);

            // when
            PointRankingListResponse response = pointService.getPointRanking(userDetails);

            // then
            assertEquals(10, response.top10().size()); // 상위 10명만
            assertEquals(13, response.memberCount()); // 전체 멤버 수

            // 첫 번째 순위 검증
            assertEquals(1L, response.top10().get(0).rank());
            assertEquals(BigDecimal.valueOf(1300), response.top10().get(0).totalEarnedPoints());

            // 10번째 순위 검증
            assertEquals(10L, response.top10().get(9).rank());
            assertEquals(BigDecimal.valueOf(400), response.top10().get(9).totalEarnedPoints());

            // 현재 사용자 랭킹 검증 (13위)
            assertEquals(13L, response.me().rank());
            assertEquals(BigDecimal.valueOf(100), response.me().totalEarnedPoints());
            assertEquals(currentUser.getName(), response.me().name());
        }

        @Test
        void 동점자_처리_테스트_13명_환경() {
            // given: 13명 생성 (동점자 3명 포함)
            Member[] members = new Member[13];
            PointAccount[] accounts = new PointAccount[13];

            for (int i = 0; i < 13; i++) {
                members[i] = createMember();
                createCompletedPaymentForCurrentSemester(members[i]);
                accounts[i] = createPointAccount(members[i]);
            }

            // 점수 설정: 1000(1명), 900(3명 동점), 800(1명), ..., 100(1명)
            createEarnPointTransaction(accounts[0], BigDecimal.valueOf(1000), "1위");
            createEarnPointTransaction(accounts[1], BigDecimal.valueOf(900), "동점자1");
            createEarnPointTransaction(accounts[2], BigDecimal.valueOf(900), "동점자2");
            createEarnPointTransaction(accounts[3], BigDecimal.valueOf(900), "동점자3");

            for (int i = 4; i < 13; i++) {
                // 800, 700, 600, 500, 400, 300, 200, 100, 100 (마지막은 100으로 고정)
                int points = Math.max(800 - ((i - 4) * 100), 100);
                createEarnPointTransaction(accounts[i], BigDecimal.valueOf(points), "적립");
            }

            Member currentUser = members[12]; // 마지막 (100포인트)
            UserDetails userDetails = createUserDetails(currentUser);

            // when
            PointRankingListResponse response = pointService.getPointRanking(userDetails);

            // then
            assertEquals(10, response.top10().size());

            // 1위 검증
            assertEquals(1L, response.top10().get(0).rank());
            assertEquals(BigDecimal.valueOf(1000), response.top10().get(0).totalEarnedPoints());

            // 동점자들은 모두 2위 (1명이 1위이므로)
            long tieRank = 2L;
            long tieCount = response.top10().stream()
                    .filter(r -> r.totalEarnedPoints().equals(BigDecimal.valueOf(900)))
                    .count();
            assertEquals(3, tieCount);

            // 동점자들의 랭킹은 모두 2위
            response.top10().stream()
                    .filter(r -> r.totalEarnedPoints().equals(BigDecimal.valueOf(900)))
                    .forEach(r -> assertEquals(tieRank, r.rank()));

            // 현재 사용자는 12위 (100포인트 동점자)
            assertEquals(12L, response.me().rank());
        }

        @Test
        void 본인이_상위10명에_포함되는_경우_13명_환경() {
            // given: 13명 생성
            Member[] members = new Member[13];
            PointAccount[] accounts = new PointAccount[13];

            for (int i = 0; i < 13; i++) {
                members[i] = createMember();
                createCompletedPaymentForCurrentSemester(members[i]);
                accounts[i] = createPointAccount(members[i]);
                createEarnPointTransaction(accounts[i], BigDecimal.valueOf(1300 - (i * 100)), "적립");
            }

            Member currentUser = members[4]; // 5번째 (900포인트) - 상위 10명 내
            UserDetails userDetails = createUserDetails(currentUser);

            // when
            PointRankingListResponse response = pointService.getPointRanking(userDetails);

            // then
            assertEquals(10, response.top10().size());

            // 본인이 상위 10명에 포함되어야 함
            boolean currentUserInTop10 =
                    response.top10().stream().anyMatch(r -> r.name().equals(currentUser.getName()));
            assertTrue(currentUserInTop10);

            // 현재 사용자 랭킹 검증 (5위)
            assertEquals(5L, response.me().rank());
            assertEquals(BigDecimal.valueOf(900), response.me().totalEarnedPoints());
            assertEquals(currentUser.getName(), response.me().name());
        }

        @Test
        void 본인이_상위10명_밖인_경우_13명_환경() {
            // given: 13명 생성
            Member[] members = new Member[13];
            PointAccount[] accounts = new PointAccount[13];

            for (int i = 0; i < 13; i++) {
                members[i] = createMember();
                createCompletedPaymentForCurrentSemester(members[i]);
                accounts[i] = createPointAccount(members[i]);
                createEarnPointTransaction(accounts[i], BigDecimal.valueOf(1300 - (i * 100)), "적립");
            }

            Member currentUser = members[11]; // 12번째 (200포인트) - 상위 10명 밖
            UserDetails userDetails = createUserDetails(currentUser);

            // when
            PointRankingListResponse response = pointService.getPointRanking(userDetails);

            // then
            assertEquals(10, response.top10().size());

            // 본인이 상위 10명에 포함되지 않아야 함
            boolean currentUserInTop10 =
                    response.top10().stream().anyMatch(r -> r.name().equals(currentUser.getName()));
            assertFalse(currentUserInTop10);

            // 현재 사용자 랭킹 검증 (12위)
            assertEquals(12L, response.me().rank());
            assertEquals(BigDecimal.valueOf(200), response.me().totalEarnedPoints());
            assertEquals(currentUser.getName(), response.me().name());
        }

        @Test
        void 포인트_거래가_없는_사용자_테스트_13명_환경() {
            // given: 12명은 포인트 있고, 1명(현재 사용자)은 포인트 없음
            Member[] members = new Member[13];
            PointAccount[] accounts = new PointAccount[13];

            for (int i = 0; i < 13; i++) {
                members[i] = createMember();
                createCompletedPaymentForCurrentSemester(members[i]);
                accounts[i] = createPointAccount(members[i]);

                // 처음 12명만 포인트 적립, 마지막 1명은 포인트 없음
                if (i < 12) {
                    createEarnPointTransaction(accounts[i], BigDecimal.valueOf(1200 - (i * 100)), "적립");
                }
            }

            Member currentUser = members[12]; // 포인트가 없는 사용자
            UserDetails userDetails = createUserDetails(currentUser);

            // when
            PointRankingListResponse response = pointService.getPointRanking(userDetails);

            // then
            assertEquals(10, response.top10().size()); // 상위 10명
            assertEquals(13, response.memberCount()); // 전체 멤버 수

            // 현재 사용자는 0포인트로 13위
            assertEquals(13L, response.me().rank());
            assertEquals(BigDecimal.ZERO, response.me().totalEarnedPoints());
            assertEquals(currentUser.getName(), response.me().name());

            // 상위 10명에는 포함되지 않음
            boolean currentUserInTop10 =
                    response.top10().stream().anyMatch(r -> r.name().equals(currentUser.getName()));
            assertFalse(currentUserInTop10);
        }
    }
}
