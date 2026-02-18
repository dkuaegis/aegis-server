package aegis.server.domain.point.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.common.idempotency.IdempotencyKeys;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.domain.PointTransactionType;
import aegis.server.domain.point.dto.request.AdminPointBatchGrantRequest;
import aegis.server.domain.point.dto.request.AdminPointGrantRequest;
import aegis.server.domain.point.dto.response.AdminPointBatchGrantMemberResultResponse;
import aegis.server.domain.point.dto.response.AdminPointBatchGrantResultResponse;
import aegis.server.domain.point.dto.response.AdminPointGrantResultResponse;
import aegis.server.domain.point.dto.response.AdminPointLedgerPageResponse;
import aegis.server.domain.point.dto.response.AdminPointMemberPointResponse;
import aegis.server.domain.point.dto.response.AdminPointMemberSearchResponse;
import aegis.server.domain.point.dto.response.PointActionResult;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.domain.point.repository.PointTransactionRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPointService {

    private static final int MEMBER_RECENT_HISTORY_SIZE = 50;

    private final PointTransactionRepository pointTransactionRepository;
    private final PointAccountRepository pointAccountRepository;
    private final MemberRepository memberRepository;
    private final PointLedger pointLedger;

    public AdminPointLedgerPageResponse getLedger(
            int page,
            int size,
            String memberKeyword,
            PointTransactionType transactionType,
            LocalDate from,
            LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
        int normalizedSize = Math.min(size, 100);
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.plusDays(1).atStartOfDay();
        String normalizedKeyword = normalizeKeyword(memberKeyword);
        PageRequest pageRequest = PageRequest.of(page, normalizedSize);

        Page<PointTransaction> ledgerPage = pointTransactionRepository.findAdminLedger(
                normalizedKeyword, transactionType, fromDateTime, toDateTime, pageRequest);
        return AdminPointLedgerPageResponse.from(ledgerPage);
    }

    public AdminPointMemberPointResponse getMemberPoint(Long memberId) {
        Member member =
                memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        PointAccount pointAccount = pointAccountRepository
                .findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_ACCOUNT_NOT_FOUND));

        List<PointTransaction> recentTransactions = pointTransactionRepository.findRecentByPointAccountId(
                memberId, PageRequest.of(0, MEMBER_RECENT_HISTORY_SIZE));
        return AdminPointMemberPointResponse.of(member, pointAccount, recentTransactions);
    }

    public List<AdminPointMemberSearchResponse> searchMembers(String keyword, int limit) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword == null) {
            return List.of();
        }
        int normalizedLimit = Math.min(limit, 50);
        return memberRepository.searchByStudentIdOrName(normalizedKeyword, PageRequest.of(0, normalizedLimit)).stream()
                .map(AdminPointMemberSearchResponse::from)
                .toList();
    }

    @Transactional
    public AdminPointGrantResultResponse grant(AdminPointGrantRequest request) {
        validateMemberExists(request.memberId());
        String reason = request.reason().trim();
        String idempotencyKey = IdempotencyKeys.forAdminManualGrant(request.requestId(), request.memberId());
        PointActionResult result =
                pointLedger.earn(request.memberId(), BigDecimal.valueOf(request.amount()), reason, idempotencyKey);
        return AdminPointGrantResultResponse.of(result, request.memberId());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AdminPointBatchGrantResultResponse grantBatch(AdminPointBatchGrantRequest request) {
        String reason = request.reason().trim();
        List<AdminPointBatchGrantMemberResultResponse> results = request.memberIds().stream()
                .map(memberId -> grantBatchSingle(request.requestId(), memberId, request.amount(), reason))
                .toList();
        return AdminPointBatchGrantResultResponse.of(results);
    }

    private AdminPointBatchGrantMemberResultResponse grantBatchSingle(
            String requestId, Long memberId, Long amount, String reason) {
        if (!memberRepository.existsById(memberId)) {
            return AdminPointBatchGrantMemberResultResponse.failure(memberId, ErrorCode.MEMBER_NOT_FOUND.name());
        }

        try {
            String idempotencyKey = IdempotencyKeys.forAdminBatchGrant(requestId, memberId);
            PointActionResult result = pointLedger.earn(memberId, BigDecimal.valueOf(amount), reason, idempotencyKey);
            if (result.applied()) {
                return AdminPointBatchGrantMemberResultResponse.success(
                        memberId, result.transactionId(), result.accountBalance());
            }
            return AdminPointBatchGrantMemberResultResponse.duplicate(memberId, result.accountBalance());
        } catch (CustomException e) {
            return AdminPointBatchGrantMemberResultResponse.failure(
                    memberId, e.getErrorCode().name());
        } catch (IllegalArgumentException e) {
            return AdminPointBatchGrantMemberResultResponse.failure(memberId, ErrorCode.BAD_REQUEST.name());
        }
    }

    private void validateMemberExists(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }
}
