package aegis.server.domain.point.dto.response;

import java.math.BigDecimal;
import java.util.List;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.domain.PointTransaction;

public record AdminPointMemberPointResponse(
        Long memberId,
        String studentId,
        String memberName,
        BigDecimal balance,
        BigDecimal totalEarned,
        List<PointTransactionResponse> recentHistory) {

    public static AdminPointMemberPointResponse of(
            Member member, PointAccount pointAccount, List<PointTransaction> recentTransactions) {
        return new AdminPointMemberPointResponse(
                member.getId(),
                member.getStudentId(),
                member.getName(),
                pointAccount.getBalance(),
                pointAccount.getTotalEarned(),
                recentTransactions.stream().map(PointTransactionResponse::from).toList());
    }
}
