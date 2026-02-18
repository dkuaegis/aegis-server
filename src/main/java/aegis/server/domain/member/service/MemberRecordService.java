package aegis.server.domain.member.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.activity.repository.ActivityParticipationRepository;
import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.MemberRecord;
import aegis.server.domain.member.domain.MemberRecordSource;
import aegis.server.domain.member.domain.Role;
import aegis.server.domain.member.dto.response.AdminMemberActivityParticipationItemResponse;
import aegis.server.domain.member.dto.response.AdminMemberRecordPageResponse;
import aegis.server.domain.member.dto.response.AdminMemberRecordSemesterOptionResponse;
import aegis.server.domain.member.dto.response.AdminMemberRecordTimelineResponse;
import aegis.server.domain.member.dto.response.AdminMemberSemesterActivityDetailResponse;
import aegis.server.domain.member.dto.response.AdminMemberStudyAttendanceItemResponse;
import aegis.server.domain.member.dto.response.AdminMemberStudyParticipationItemResponse;
import aegis.server.domain.member.dto.response.MemberRecordBackfillResponse;
import aegis.server.domain.member.repository.MemberRecordRepository;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.domain.study.repository.StudyAttendanceRepository;
import aegis.server.domain.study.repository.StudyMemberRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberRecordService {

    private static final int BACKFILL_CHUNK_SIZE = 500;
    private static final int MAX_PAGE_SIZE = 100;

    private final MemberRecordRepository memberRecordRepository;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final MemberRecordCreator memberRecordCreator;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyAttendanceRepository studyAttendanceRepository;
    private final ActivityParticipationRepository activityParticipationRepository;

    public AdminMemberRecordPageResponse getMemberRecordsByYearSemester(YearSemester yearSemester, int page, int size) {
        return getMemberRecordsByYearSemester(yearSemester, page, size, null, null, null);
    }

    public AdminMemberRecordPageResponse getMemberRecordsByYearSemester(
            YearSemester yearSemester, int page, int size, String keyword, Role role, String sort) {
        int normalizedSize = Math.min(size, MAX_PAGE_SIZE);
        String normalizedKeyword = normalizeKeyword(keyword);
        Sort sortSpec = resolveSort(sort);

        Page<MemberRecord> memberRecordPage = memberRecordRepository.searchByYearSemesterForAdmin(
                yearSemester, normalizedKeyword, role, PageRequest.of(page, normalizedSize, sortSpec));
        return AdminMemberRecordPageResponse.from(memberRecordPage);
    }

    public List<AdminMemberRecordTimelineResponse> getMemberRecordTimeline(Long memberId) {
        validateMemberExists(memberId);
        return memberRecordRepository.findByMemberIdOrderByYearSemesterDescIdDesc(memberId).stream()
                .map(AdminMemberRecordTimelineResponse::from)
                .toList();
    }

    public List<AdminMemberRecordSemesterOptionResponse> getMemberRecordSemesterOptions() {
        return Arrays.stream(YearSemester.values())
                .map(yearSemester ->
                        AdminMemberRecordSemesterOptionResponse.of(yearSemester, yearSemester == CURRENT_YEAR_SEMESTER))
                .toList();
    }

    public AdminMemberSemesterActivityDetailResponse getMemberSemesterActivityDetail(
            Long memberId, YearSemester yearSemester) {
        validateMemberExists(memberId);

        List<AdminMemberStudyParticipationItemResponse> studyParticipations =
                studyMemberRepository
                        .findByMemberIdAndStudyYearSemesterOrderByCreatedAtDescIdDesc(memberId, yearSemester)
                        .stream()
                        .map(AdminMemberStudyParticipationItemResponse::from)
                        .toList();

        List<AdminMemberStudyAttendanceItemResponse> studyAttendances = studyAttendanceRepository
                .findByMemberIdAndStudySessionStudyYearSemesterOrderByCreatedAtDescIdDesc(memberId, yearSemester)
                .stream()
                .map(AdminMemberStudyAttendanceItemResponse::from)
                .toList();

        List<AdminMemberActivityParticipationItemResponse> activityParticipations =
                activityParticipationRepository
                        .findByMemberIdAndActivityYearSemesterOrderByCreatedAtDescIdDesc(memberId, yearSemester)
                        .stream()
                        .map(AdminMemberActivityParticipationItemResponse::from)
                        .toList();

        return AdminMemberSemesterActivityDetailResponse.of(
                memberId, yearSemester, studyParticipations, studyAttendances, activityParticipations);
    }

    @Transactional
    public MemberRecordBackfillResponse backfillFromCompletedPayments() {
        long totalCompletedPayments = 0L;
        long createdRecords = 0L;
        long skippedRecords = 0L;
        long lastPaymentId = 0L;

        while (true) {
            List<Payment> completedPayments = paymentRepository.findByStatusAndIdGreaterThanOrderByIdAsc(
                    PaymentStatus.COMPLETED, lastPaymentId, PageRequest.of(0, BACKFILL_CHUNK_SIZE));
            if (completedPayments.isEmpty()) {
                break;
            }

            for (Payment completedPayment : completedPayments) {
                totalCompletedPayments++;
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
                lastPaymentId = completedPayment.getId();
            }
        }

        return MemberRecordBackfillResponse.of(totalCompletedPayments, createdRecords, skippedRecords);
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

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmedKeyword = keyword.trim();
        return trimmedKeyword.isEmpty() ? null : trimmedKeyword;
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "id");
        }

        return switch (sort.trim().toLowerCase()) {
            case "id,asc" -> Sort.by(Sort.Direction.ASC, "id");
            case "id,desc" -> Sort.by(Sort.Direction.DESC, "id");
            case "name,asc" -> Sort.by(Sort.Direction.ASC, "snapshotName").and(Sort.by(Sort.Direction.ASC, "id"));
            case "name,desc" -> Sort.by(Sort.Direction.DESC, "snapshotName").and(Sort.by(Sort.Direction.DESC, "id"));
            default -> throw new CustomException(ErrorCode.BAD_REQUEST);
        };
    }
}
