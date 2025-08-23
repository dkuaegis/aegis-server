package aegis.server.domain.point.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.repository.PaymentRepository;
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

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointAccountRepository pointAccountRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PaymentRepository paymentRepository;

    public PointSummaryResponse getPointSummary(UserDetails userDetails) {
        PointAccount pointAccount = pointAccountRepository
                .findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_ACCOUNT_NOT_FOUND));

        List<PointTransaction> pointTransactions =
                pointTransactionRepository.findAllByPointAccountId(userDetails.getMemberId());

        return PointSummaryResponse.from(pointAccount, pointTransactions);
    }

    public PointRankingListResponse getPointRanking(UserDetails userDetails) {
        // 현재 학기 회원 수 조회
        long memberCount = paymentRepository.countCompletedPaymentsInCurrentYearSemester();

        // 상위 10명 조회 (결제 완료자만)
        List<PointAccount> top10Accounts = pointAccountRepository.findTopByEligible(
                CURRENT_YEAR_SEMESTER, PaymentStatus.COMPLETED, PageRequest.of(0, 10));
        List<PointRankingResponse> top10Rankings = convertToRankingResponses(top10Accounts);

        // 현재 사용자 랭킹 계산 (결제 완료자 기준)
        PointRankingResponse currentUserRanking = getCurrentUserRanking(userDetails.getMemberId());

        return PointRankingListResponse.of(memberCount, top10Rankings, currentUserRanking);
    }

    private PointRankingResponse getCurrentUserRanking(Long memberId) {
        PointAccount currentUserAccount = pointAccountRepository
                .findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_ACCOUNT_NOT_FOUND));

        BigDecimal userTotalEarned = currentUserAccount.getTotalEarned();
        long higherCount = pointAccountRepository.countEligibleWithTotalEarnedGreaterThan(
                CURRENT_YEAR_SEMESTER, PaymentStatus.COMPLETED, userTotalEarned);
        Long rank = higherCount + 1;

        return PointRankingResponse.of(
                rank,
                currentUserAccount.getMember().getName(),
                userTotalEarned,
                currentUserAccount.getMember().getProfileIcon());
    }

    private List<PointRankingResponse> convertToRankingResponses(List<PointAccount> accounts) {
        if (accounts.isEmpty()) {
            return List.of();
        }

        List<PointRankingResponse> responses = new ArrayList<>();
        long rank = 1;
        BigDecimal previousTotalEarned = null;

        for (int i = 0; i < accounts.size(); i++) {
            PointAccount account = accounts.get(i);
            BigDecimal totalEarned = account.getTotalEarned();

            // 동점자 처리: 이전 점수와 다르면 현재 인덱스 + 1을 등수로 설정
            if (previousTotalEarned != null && totalEarned.compareTo(previousTotalEarned) < 0) {
                rank = i + 1;
            }

            responses.add(PointRankingResponse.of(
                    rank,
                    account.getMember().getName(),
                    totalEarned,
                    account.getMember().getProfileIcon()));

            previousTotalEarned = totalEarned;
        }

        return responses;
    }
}
