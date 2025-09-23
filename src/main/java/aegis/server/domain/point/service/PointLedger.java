package aegis.server.domain.point.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.domain.PointTransactionType;
import aegis.server.domain.point.dto.response.PointActionResult;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.domain.point.repository.PointTransactionRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointLedger {

    private final PointAccountRepository pointAccountRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Transactional
    public PointActionResult earn(Long memberId, BigDecimal amount, String reason, String idempotencyKey) {
        // 1) 계좌 비관적 락
        PointAccount account = pointAccountRepository
                .findByIdWithLock(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_ACCOUNT_NOT_FOUND));

        // 2) 멱등키 선조회
        if (pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            return PointActionResult.of(null, account.getBalance(), false);
        }

        // 3) 적립 및 트랜잭션 기록
        account.add(amount);
        PointTransaction tx =
                PointTransaction.create(account, PointTransactionType.EARN, amount, reason, idempotencyKey);
        pointTransactionRepository.save(tx);
        return PointActionResult.of(tx.getId(), account.getBalance(), true);
    }

    @Transactional
    public PointActionResult spend(Long memberId, BigDecimal amount, String reason) {
        // 1) 계좌 비관적 락
        PointAccount account = pointAccountRepository
                .findByIdWithLock(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_ACCOUNT_NOT_FOUND));

        // 2) 차감 및 트랜잭션 기록
        account.deduct(amount);
        PointTransaction tx = PointTransaction.create(account, PointTransactionType.SPEND, amount, reason);
        pointTransactionRepository.save(tx);
        return PointActionResult.of(tx.getId(), account.getBalance(), true);
    }
}
