package aegis.server.domain.pointshop.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.domain.PointTransactionType;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.domain.point.repository.PointTransactionRepository;
import aegis.server.domain.pointshop.domain.PointShopDrawHistory;
import aegis.server.domain.pointshop.domain.PointShopItem;
import aegis.server.domain.pointshop.domain.event.PointShopDrawnEvent;
import aegis.server.domain.pointshop.dto.internal.PointShopDrawInfo;
import aegis.server.domain.pointshop.dto.response.PointShopDrawHistoryResponse;
import aegis.server.domain.pointshop.dto.response.PointShopDrawResponse;
import aegis.server.domain.pointshop.repository.PointShopDrawHistoryRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointShopService {

    private static final BigDecimal DRAW_COST = BigDecimal.valueOf(100L);

    private final PointAccountRepository pointAccountRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointShopDrawHistoryRepository pointShopDrawHistoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public List<PointShopDrawHistoryResponse> getMyDrawHistories(UserDetails userDetails) {
        List<PointShopDrawHistory> histories =
                pointShopDrawHistoryRepository.findAllByMemberIdOrderByIdDesc(userDetails.getMemberId());
        return histories.stream().map(PointShopDrawHistoryResponse::from).toList();
    }

    @Transactional
    public PointShopDrawResponse draw(UserDetails userDetails) {
        // 1. 비관적 락과 함께 포인트 계좌 조회
        PointAccount account = pointAccountRepository
                .findByIdWithLock(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_ACCOUNT_NOT_FOUND));

        // 2. 잔액 차감
        account.deduct(DRAW_COST);
        pointAccountRepository.save(account);

        // 3. 포인트 트랜잭션 생성
        PointTransaction transaction =
                PointTransaction.create(account, PointTransactionType.SPEND, DRAW_COST, "포인트샵 뽑기");
        pointTransactionRepository.save(transaction);

        // 4. 가중치 기반 추첨
        PointShopItem drawnItem = drawItem();

        // 5. 뽑은 상품 이력 저장
        PointShopDrawHistory history = PointShopDrawHistory.create(account.getMember(), drawnItem, transaction);
        pointShopDrawHistoryRepository.save(history);

        // 6. 이벤트 발행
        applicationEventPublisher.publishEvent(new PointShopDrawnEvent(PointShopDrawInfo.from(history)));

        // 7. 응답 반환
        return PointShopDrawResponse.of(drawnItem, account.getBalance(), transaction.getId(), history.getId());
    }

    private PointShopItem drawItem() {
        PointShopItem[] items = PointShopItem.values();
        int total = Arrays.stream(items).mapToInt(PointShopItem::getWeight).sum();
        int r = ThreadLocalRandom.current().nextInt(1, total + 1);
        int cum = 0;
        for (PointShopItem item : items) {
            cum += item.getWeight();
            if (r <= cum) return item;
        }
        throw new IllegalStateException("Unreachable");
    }
}
