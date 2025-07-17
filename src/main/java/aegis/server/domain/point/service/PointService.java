package aegis.server.domain.point.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.domain.PointTransaction;
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
}
