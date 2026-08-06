package aegis.server.domain.qrcode.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.qrcode.domain.QRCode;
import aegis.server.domain.qrcode.repository.QRCodeRepository;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTestWithoutTransactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("concurrency")
class QRCodeIssueConcurrencyTest extends IntegrationTestWithoutTransactional {

    private static final int REQUEST_COUNT = 10;

    @Autowired
    QRCodeService qrCodeService;

    @Autowired
    QRCodeRepository qrCodeRepository;

    @Test
    void 동시에_QR코드를_발급해도_회원당_하나만_유지한다() {
        // given
        Member member = createMember();
        UserDetails userDetails = createUserDetails(member);
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(REQUEST_COUNT);

        try {
            List<CompletableFuture<Void>> requests = IntStream.range(0, REQUEST_COUNT)
                    .mapToObj(index -> CompletableFuture.runAsync(
                            () -> issueAfterSignal(startLatch, userDetails), executorService))
                    .toList();

            // when
            startLatch.countDown();
            CompletableFuture.allOf(requests.toArray(new CompletableFuture[0])).join();

            // then
            QRCode savedQRCode = qrCodeRepository.findByMemberId(member.getId()).orElseThrow();
            assertEquals(member.getId(), savedQRCode.getMemberId());
            assertEquals(1, qrCodeRepository.count());
        } finally {
            executorService.shutdown();
        }
    }

    private void issueAfterSignal(CountDownLatch startLatch, UserDetails userDetails) {
        try {
            startLatch.await();
            qrCodeService.issueQRCode(userDetails);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("QR 코드 발급 동시성 테스트가 중단되었습니다.", e);
        }
    }
}
