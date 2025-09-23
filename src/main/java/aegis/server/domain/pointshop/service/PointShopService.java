package aegis.server.domain.pointshop.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.dto.response.PointActionResult;
import aegis.server.domain.point.repository.PointTransactionRepository;
import aegis.server.domain.point.service.PointLedger;
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

    private final PointLedger pointLedger;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointShopDrawHistoryRepository pointShopDrawHistoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MemberRepository memberRepository;

    public List<PointShopDrawHistoryResponse> getMyDrawHistories(UserDetails userDetails) {
        List<PointShopDrawHistory> histories =
                pointShopDrawHistoryRepository.findAllByMemberIdOrderByIdDesc(userDetails.getMemberId());
        return histories.stream().map(PointShopDrawHistoryResponse::from).toList();
    }

    @Transactional
    public PointShopDrawResponse draw(UserDetails userDetails) {
        // 1. 포인트 차감
        Long memberId = userDetails.getMemberId();
        PointActionResult pointActionResult = pointLedger.spend(memberId, DRAW_COST, "포인트샵 뽑기");

        // 2. 가중치 기반 추첨
        PointShopItem drawnItem = drawItem();

        // 3. 뽑은 상품 이력 저장
        PointTransaction transaction = pointTransactionRepository
                .findById(pointActionResult.transactionId())
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_TRANSACTION_NOT_FOUND));
        Member member = memberRepository
                .findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        PointShopDrawHistory history = PointShopDrawHistory.create(member, drawnItem, transaction);
        pointShopDrawHistoryRepository.save(history);

        // 4. 이벤트 발행
        applicationEventPublisher.publishEvent(new PointShopDrawnEvent(PointShopDrawInfo.from(history)));

        // 5. 응답 반환
        return PointShopDrawResponse.of(
                drawnItem, pointActionResult.accountBalance(), pointActionResult.transactionId(), history.getId());
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
