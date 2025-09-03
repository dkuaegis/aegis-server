package aegis.server.domain.pointshop.service;

import java.math.BigDecimal;
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

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.domain.point.repository.PointTransactionRepository;
import aegis.server.domain.pointshop.repository.PointShopDrawHistoryRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTestWithoutTransactional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("concurrency")
@ActiveProfiles("postgres")
class PointShopDrawConcurrencyTest extends IntegrationTestWithoutTransactional {

    @Autowired
    PointShopService pointShopService;

    @Autowired
    PointAccountRepository pointAccountRepository;

    @Autowired
    PointTransactionRepository pointTransactionRepository;

    @Autowired
    PointShopDrawHistoryRepository pointShopDrawHistoryRepository;

    @Test
    void 동시에_같은_사용자가_여러번_뽑기_요청해도_잔액_이상_차감되지_않는다() throws InterruptedException {
        // given
        Member member = createMember();
        UserDetails userDetails = createUserDetails(member);

        PointAccount account = pointAccountRepository.save(PointAccount.create(member));
        int cost = 100;
        int allowedDraws = 20; // 2,000 포인트면 20회 가능
        account.add(BigDecimal.valueOf((long) cost * allowedDraws));
        pointAccountRepository.save(account);

        int threads = 200; // 요청 수

        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger insufficientCount = new AtomicInteger(0);

        // when
        List<CompletableFuture<Void>> futures = IntStream.range(0, threads)
                .mapToObj(i -> CompletableFuture.runAsync(
                        () -> {
                            try {
                                startLatch.await();
                                pointShopService.draw(userDetails);
                                successCount.incrementAndGet();
                            } catch (CustomException e) {
                                if (e.getErrorCode() == ErrorCode.POINT_INSUFFICIENT_BALANCE) {
                                    insufficientCount.incrementAndGet();
                                } else {
                                    throw new RuntimeException("예상하지 못한 예외 발생: " + e.getErrorCode());
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            } finally {
                                endLatch.countDown();
                            }
                        },
                        executorService))
                .toList();

        startLatch.countDown();
        endLatch.await();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();

        // then
        PointAccount updated = pointAccountRepository.findById(member.getId()).get();
        assertEquals(BigDecimal.ZERO, updated.getBalance());
        assertEquals(allowedDraws, successCount.get());
        assertEquals(threads - allowedDraws, insufficientCount.get());

        // DB 검증: 트랜잭션/이력 개수 = 성공 횟수
        assertEquals(
                allowedDraws,
                pointTransactionRepository
                        .findAllByPointAccountId(member.getId())
                        .size());
        assertEquals(
                allowedDraws,
                pointShopDrawHistoryRepository.findAllByMemberId(member.getId()).size());
    }
}
