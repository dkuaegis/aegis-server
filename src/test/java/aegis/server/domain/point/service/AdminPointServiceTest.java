package aegis.server.domain.point.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.*;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.domain.PointTransactionType;
import aegis.server.domain.point.dto.request.AdminPointBatchGrantRequest;
import aegis.server.domain.point.dto.request.AdminPointGrantRequest;
import aegis.server.domain.point.dto.response.*;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.domain.point.repository.PointTransactionRepository;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

class AdminPointServiceTest extends IntegrationTest {

    @Autowired
    AdminPointService adminPointService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PointAccountRepository pointAccountRepository;

    @Autowired
    PointTransactionRepository pointTransactionRepository;

    @Nested
    class 통합_원장_조회 {

        @Test
        void 회원키워드와_거래유형으로_필터링할_수_있다() {
            // given
            Member memberA = createMember();
            memberA.updatePersonalInfo(
                    "010-0000-0001", "32001111", Department.SW융합대학_컴퓨터공학과, Grade.THREE, "010101", Gender.MALE);
            memberRepository.save(memberA);

            Member memberB = createMember();
            memberB.updatePersonalInfo(
                    "010-0000-0002", "32002222", Department.SW융합대학_컴퓨터공학과, Grade.THREE, "010101", Gender.MALE);
            memberRepository.save(memberB);

            PointAccount accountA = createPointAccount(memberA);
            PointAccount accountB = createPointAccount(memberB);

            createEarnPointTransaction(accountA, BigDecimal.valueOf(100), "A 적립");
            createSpendPointTransaction(accountA, BigDecimal.valueOf(30), "A 사용");
            createEarnPointTransaction(accountB, BigDecimal.valueOf(200), "B 적립");

            // when
            AdminPointLedgerPageResponse response =
                    adminPointService.getLedger(0, 50, "32001111", PointTransactionType.EARN, null, null);

            // then
            assertEquals(1, response.content().size());
            assertEquals(memberA.getId(), response.content().getFirst().memberId());
            assertEquals(
                    PointTransactionType.EARN, response.content().getFirst().transactionType());
            assertEquals("A 적립", response.content().getFirst().reason());
        }

        @Test
        void 회원키워드가_null이거나_공백이면_키워드_필터_없이_조회한다() {
            // given
            Member memberA = createMember();
            memberA.updateName("홍길동");
            memberA.updatePersonalInfo(
                    "010-0000-0001", "32001111", Department.SW융합대학_컴퓨터공학과, Grade.THREE, "010101", Gender.MALE);
            memberRepository.save(memberA);

            Member memberB = createMember();
            memberB.updateName("김영희");
            memberB.updatePersonalInfo(
                    "010-0000-0002", "32002222", Department.SW융합대학_컴퓨터공학과, Grade.THREE, "010101", Gender.FEMALE);
            memberRepository.save(memberB);

            PointAccount accountA = createPointAccount(memberA);
            PointAccount accountB = createPointAccount(memberB);
            createEarnPointTransaction(accountA, BigDecimal.valueOf(100), "A 적립");
            createEarnPointTransaction(accountB, BigDecimal.valueOf(200), "B 적립");

            // when
            AdminPointLedgerPageResponse nullKeywordResponse =
                    adminPointService.getLedger(0, 50, null, null, null, null);
            AdminPointLedgerPageResponse blankKeywordResponse =
                    adminPointService.getLedger(0, 50, "   ", null, null, null);

            // then
            assertTrue(nullKeywordResponse.content().size() >= 2);
            assertEquals(
                    nullKeywordResponse.content().size(),
                    blankKeywordResponse.content().size());
            assertTrue(nullKeywordResponse.content().stream()
                    .anyMatch(item -> item.memberId().equals(memberA.getId())));
            assertTrue(nullKeywordResponse.content().stream()
                    .anyMatch(item -> item.memberId().equals(memberB.getId())));
            assertTrue(blankKeywordResponse.content().stream()
                    .anyMatch(item -> item.memberId().equals(memberA.getId())));
            assertTrue(blankKeywordResponse.content().stream()
                    .anyMatch(item -> item.memberId().equals(memberB.getId())));
        }
    }

    @Nested
    class 회원별_조회 {

        @Test
        void 회원_포인트_상세를_조회한다() {
            // given
            Member member = createMember();
            PointAccount account = createPointAccount(member);
            createEarnPointTransaction(account, BigDecimal.valueOf(300), "테스트 적립");
            createSpendPointTransaction(account, BigDecimal.valueOf(50), "테스트 사용");

            // when
            AdminPointMemberPointResponse response = adminPointService.getMemberPoint(member.getId());

            // then
            assertEquals(member.getId(), response.memberId());
            assertEquals(BigDecimal.valueOf(250), response.balance());
            assertEquals(BigDecimal.valueOf(300), response.totalEarned());
            assertEquals(2, response.recentHistory().size());
            assertEquals("테스트 사용", response.recentHistory().getFirst().reason());
        }
    }

    @Nested
    class 수동_지급 {

        @Test
        void 단건_수동_지급에_성공한다() {
            // given
            Member member = createMember();
            PointAccount account = createPointAccount(member);
            BigDecimal beforeBalance = account.getBalance();

            AdminPointGrantRequest request =
                    new AdminPointGrantRequest(UUID.randomUUID().toString(), member.getId(), 150L, "운영 보정 지급");

            // when
            AdminPointGrantResultResponse response = adminPointService.grant(request);

            // then 반환값 검증
            assertTrue(response.created());
            assertNotNull(response.pointTransactionId());
            assertEquals(member.getId(), response.memberId());

            // then DB 상태 검증
            PointTransaction transaction = pointTransactionRepository
                    .findById(response.pointTransactionId())
                    .orElseThrow();
            assertEquals(BigDecimal.valueOf(150), transaction.getAmount());
            assertEquals("운영 보정 지급", transaction.getReason());
            assertEquals(PointTransactionType.EARN, transaction.getTransactionType());

            PointAccount refreshed =
                    pointAccountRepository.findByMemberId(member.getId()).orElseThrow();
            assertEquals(beforeBalance.add(BigDecimal.valueOf(150)), refreshed.getBalance());
            assertEquals(BigDecimal.valueOf(150), refreshed.getTotalEarned());
        }

        @Test
        void 단건_수동_지급은_동일_requestId_재요청시_중복지급되지_않는다() {
            // given
            Member member = createMember();
            createPointAccount(member);
            String requestId = UUID.randomUUID().toString();
            AdminPointGrantRequest request = new AdminPointGrantRequest(requestId, member.getId(), 100L, "중복 방지");

            // when
            AdminPointGrantResultResponse first = adminPointService.grant(request);
            AdminPointGrantResultResponse second = adminPointService.grant(request);

            // then
            assertTrue(first.created());
            assertFalse(second.created());
            assertNull(second.pointTransactionId());

            List<PointTransaction> transactions = pointTransactionRepository.findAllByPointAccountId(member.getId());
            assertEquals(1, transactions.size());
            assertEquals(BigDecimal.valueOf(100), transactions.getFirst().getAmount());

            PointAccount refreshed =
                    pointAccountRepository.findByMemberId(member.getId()).orElseThrow();
            assertEquals(BigDecimal.valueOf(100), refreshed.getBalance());
        }

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void 일괄_수동_지급은_성공_중복_실패를_함께_반환한다() {
            // given
            Member memberA = createMember();
            Member memberB = createMember();
            createPointAccount(memberA);
            createPointAccount(memberB);

            List<Long> memberIds = List.of(memberA.getId(), memberA.getId(), 999_999L, memberB.getId());
            AdminPointBatchGrantRequest request =
                    new AdminPointBatchGrantRequest(UUID.randomUUID().toString(), memberIds, 30L, "배치 지급");

            // when
            AdminPointBatchGrantResultResponse response = adminPointService.grantBatch(request);

            // then
            assertEquals(4, response.totalRequested());
            assertEquals(2, response.successCount());
            assertEquals(1, response.duplicateCount());
            assertEquals(1, response.failureCount());

            assertEquals(
                    AdminPointBatchGrantStatus.SUCCESS,
                    response.results().get(0).status());
            assertEquals(
                    AdminPointBatchGrantStatus.DUPLICATE,
                    response.results().get(1).status());
            assertEquals(
                    AdminPointBatchGrantStatus.FAILED, response.results().get(2).status());
            assertEquals(
                    AdminPointBatchGrantStatus.SUCCESS,
                    response.results().get(3).status());

            PointAccount refreshedA =
                    pointAccountRepository.findByMemberId(memberA.getId()).orElseThrow();
            PointAccount refreshedB =
                    pointAccountRepository.findByMemberId(memberB.getId()).orElseThrow();
            assertEquals(BigDecimal.valueOf(30), refreshedA.getBalance());
            assertEquals(BigDecimal.valueOf(30), refreshedB.getBalance());
        }
    }

    @Nested
    class 회원_검색 {

        @Test
        void 학번과_이름으로_회원을_검색할_수_있다() {
            // given
            Member memberA = createMember();
            memberA.updateName("홍길동");
            memberA.updatePersonalInfo(
                    "010-0000-0001", "32004567", Department.SW융합대학_컴퓨터공학과, Grade.THREE, "010101", Gender.MALE);
            memberRepository.save(memberA);

            Member memberB = createMember();
            memberB.updateName("김영희");
            memberB.updatePersonalInfo(
                    "010-0000-0002", "32009999", Department.SW융합대학_컴퓨터공학과, Grade.THREE, "010101", Gender.FEMALE);
            memberRepository.save(memberB);

            // when
            List<AdminPointMemberSearchResponse> byName = adminPointService.searchMembers("홍길", 20);
            List<AdminPointMemberSearchResponse> byStudentId = adminPointService.searchMembers("4567", 20);

            // then
            assertTrue(byName.stream().anyMatch(member -> member.memberId().equals(memberA.getId())));
            assertTrue(byStudentId.stream().anyMatch(member -> member.memberId().equals(memberA.getId())));
            assertFalse(
                    byStudentId.stream().anyMatch(member -> member.memberId().equals(memberB.getId())));
        }
    }
}
