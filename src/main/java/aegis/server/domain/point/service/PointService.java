package aegis.server.domain.point.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.dto.response.PointRankingListResponse;
import aegis.server.domain.point.dto.response.PointRankingResponse;
import aegis.server.domain.point.dto.response.PointSummaryResponse;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.domain.point.repository.PointTransactionRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointAccountRepository pointAccountRepository;
    private final PointTransactionRepository pointTransactionRepository;

    public PointSummaryResponse getPointSummary(UserDetails userDetails) {
        PointAccount pointAccount = pointAccountRepository
                .findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_ACCOUNT_NOT_FOUND));

        List<PointTransaction> pointTransactions =
                pointTransactionRepository.findAllByPointAccountId(userDetails.getMemberId());

        return PointSummaryResponse.from(pointAccount, pointTransactions);
    }

    public PointRankingListResponse getPointRanking(UserDetails userDetails) {
        // 전체 회원 수 조회
        long memberCount = pointAccountRepository.count();

        // 상위 10명 조회
        List<PointAccount> top10Accounts = pointAccountRepository.findTop10ByTotalEarned();
        List<PointRankingResponse> top10Rankings =
                top10Accounts.stream().map(this::convertToRankingResponse).toList();

        // 현재 사용자 랭킹 계산
        PointRankingResponse currentUserRanking = getCurrentUserRanking(userDetails.getMemberId());

        return PointRankingListResponse.of(memberCount, top10Rankings, currentUserRanking);
    }

    private PointRankingResponse getCurrentUserRanking(Long memberId) {
        PointAccount currentUserAccount = pointAccountRepository
                .findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_ACCOUNT_NOT_FOUND));

        BigDecimal userTotalEarned = currentUserAccount.getTotalEarned();
        Long rank = pointAccountRepository.countByTotalEarnedGreaterThan(userTotalEarned) + 1;

        return PointRankingResponse.of(
                rank,
                currentUserAccount.getMember().getName(),
                userTotalEarned,
                currentUserAccount.getMember().getProfileIcon());
    }

    private PointRankingResponse convertToRankingResponse(PointAccount account) {
        BigDecimal totalEarned = account.getTotalEarned();
        Long rank = pointAccountRepository.countByTotalEarnedGreaterThan(totalEarned) + 1;

        return PointRankingResponse.of(
                rank,
                account.getMember().getName(),
                totalEarned,
                account.getMember().getProfileIcon());
    }
}
