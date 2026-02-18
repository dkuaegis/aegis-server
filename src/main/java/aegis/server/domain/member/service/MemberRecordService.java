package aegis.server.domain.member.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.MemberRecord;
import aegis.server.domain.member.domain.MemberRecordSource;
import aegis.server.domain.member.dto.response.AdminMemberRecordPageResponse;
import aegis.server.domain.member.dto.response.AdminMemberRecordTimelineResponse;
import aegis.server.domain.member.dto.response.MemberRecordBackfillResponse;
import aegis.server.domain.member.repository.MemberRecordRepository;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberRecordService {

    private static final int MAX_PAGE_SIZE = 100;

    private final MemberRecordRepository memberRecordRepository;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final MemberRecordCreator memberRecordCreator;

    public AdminMemberRecordPageResponse getMemberRecordsByYearSemester(YearSemester yearSemester, int page, int size) {
        int normalizedSize = Math.min(size, MAX_PAGE_SIZE);
        Page<MemberRecord> memberRecordPage = memberRecordRepository.findByYearSemesterOrderByIdAsc(
                yearSemester, PageRequest.of(page, normalizedSize));
        return AdminMemberRecordPageResponse.from(memberRecordPage);
    }

    public List<AdminMemberRecordTimelineResponse> getMemberRecordTimeline(Long memberId) {
        validateMemberExists(memberId);
        return memberRecordRepository.findByMemberIdOrderByYearSemesterDescIdDesc(memberId).stream()
                .map(AdminMemberRecordTimelineResponse::from)
                .toList();
    }

    @Transactional
    public MemberRecordBackfillResponse backfillFromCompletedPayments() {
        List<Payment> completedPayments = paymentRepository.findAllByStatusFetchMember(PaymentStatus.COMPLETED);

        long createdRecords = 0L;
        long skippedRecords = 0L;

        for (Payment completedPayment : completedPayments) {
            boolean created = createMemberRecordIfAbsent(
                    completedPayment.getMember(),
                    completedPayment.getYearSemester(),
                    MemberRecordSource.BACKFILL_PAYMENT,
                    completedPayment.getId(),
                    completedPayment.getUpdatedAt());
            if (created) {
                createdRecords++;
            } else {
                skippedRecords++;
            }
        }

        return MemberRecordBackfillResponse.of(completedPayments.size(), createdRecords, skippedRecords);
    }

    @Transactional
    public boolean createMemberRecordIfAbsent(
            Long memberId, YearSemester yearSemester, MemberRecordSource recordSource) {
        return createMemberRecordIfAbsent(memberId, yearSemester, recordSource, null, null);
    }

    @Transactional
    public boolean createMemberRecordIfAbsent(
            Long memberId,
            YearSemester yearSemester,
            MemberRecordSource recordSource,
            Long paymentId,
            LocalDateTime paymentCompletedAt) {
        Member member =
                memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return createMemberRecordIfAbsent(member, yearSemester, recordSource, paymentId, paymentCompletedAt);
    }

    private boolean createMemberRecordIfAbsent(
            Member member,
            YearSemester yearSemester,
            MemberRecordSource recordSource,
            Long paymentId,
            LocalDateTime paymentCompletedAt) {
        return memberRecordCreator.createIfAbsent(member, yearSemester, recordSource, paymentId, paymentCompletedAt);
    }

    private void validateMemberExists(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }
}
