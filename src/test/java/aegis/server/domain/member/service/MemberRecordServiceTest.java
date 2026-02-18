package aegis.server.domain.member.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Department;
import aegis.server.domain.member.domain.Gender;
import aegis.server.domain.member.domain.Grade;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.MemberRecord;
import aegis.server.domain.member.domain.MemberRecordSource;
import aegis.server.domain.member.domain.Role;
import aegis.server.domain.member.dto.response.AdminMemberRecordPageResponse;
import aegis.server.domain.member.dto.response.AdminMemberRecordTimelineResponse;
import aegis.server.domain.member.dto.response.MemberRecordBackfillResponse;
import aegis.server.domain.member.repository.MemberRecordRepository;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.member.service.listener.MemberRecordEventListener;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.domain.event.PaymentCompletedEvent;
import aegis.server.domain.payment.dto.internal.PaymentInfo;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.helper.IntegrationTestWithoutTransactional;

import static org.junit.jupiter.api.Assertions.*;

class MemberRecordServiceTest extends IntegrationTestWithoutTransactional {

    @Autowired
    MemberRecordService memberRecordService;

    @Autowired
    MemberRecordEventListener memberRecordEventListener;

    @Autowired
    MemberRecordRepository memberRecordRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Nested
    class 결제완료_이벤트_기록 {

        @Test
        void 동일_학기_결제완료_이벤트는_한건만_기록된다() {
            // given
            Member member = createMember();
            Payment payment = paymentRepository.saveAndFlush(
                    Payment.createForDev(member, PaymentStatus.COMPLETED, YearSemester.YEAR_SEMESTER_2026_1));

            // when
            PaymentCompletedEvent event = new PaymentCompletedEvent(PaymentInfo.from(payment));
            memberRecordEventListener.handlePaymentCompletedEvent(event);
            memberRecordEventListener.handlePaymentCompletedEvent(event);

            // then
            List<MemberRecord> records =
                    memberRecordRepository.findByMemberIdOrderByYearSemesterDescIdDesc(member.getId());
            assertEquals(1, records.size());
            assertEquals(YearSemester.YEAR_SEMESTER_2026_1, records.getFirst().getYearSemester());
            assertEquals(
                    MemberRecordSource.PAYMENT_COMPLETED, records.getFirst().getRecordSource());
            assertEquals(member.getStudentId(), records.getFirst().getSnapshotStudentId());
            assertEquals(member.getName(), records.getFirst().getSnapshotName());
            assertEquals(member.getEmail(), records.getFirst().getSnapshotEmail());
            assertEquals(member.getPhoneNumber(), records.getFirst().getSnapshotPhoneNumber());
            assertEquals(member.getDepartment(), records.getFirst().getSnapshotDepartment());
            assertEquals(member.getGrade(), records.getFirst().getSnapshotGrade());
            assertEquals(member.getRole(), records.getFirst().getSnapshotRole());
            assertEquals(payment.getId(), records.getFirst().getPaymentId());
            assertEquals(payment.getUpdatedAt(), records.getFirst().getPaymentCompletedAt());
        }
    }

    @Nested
    class 결제완료_백필 {

        @Test
        void 결제완료_데이터를_기준으로_회원기록을_생성한다() {
            // given
            Member paidMember = createMember();
            Member unpaidMember = createMember();

            paymentRepository.save(
                    Payment.createForDev(paidMember, PaymentStatus.COMPLETED, YearSemester.YEAR_SEMESTER_2025_1));
            paymentRepository.save(
                    Payment.createForDev(paidMember, PaymentStatus.COMPLETED, YearSemester.YEAR_SEMESTER_2025_2));
            paymentRepository.save(
                    Payment.createForDev(unpaidMember, PaymentStatus.PENDING, YearSemester.YEAR_SEMESTER_2026_1));

            // when
            MemberRecordBackfillResponse response = memberRecordService.backfillFromCompletedPayments();

            // then 반환값 검증
            assertEquals(2, response.totalCompletedPayments());
            assertEquals(2, response.createdRecords());
            assertEquals(0, response.skippedRecords());

            // then DB 상태 검증
            List<MemberRecord> paidMemberRecords =
                    memberRecordRepository.findByMemberIdOrderByYearSemesterDescIdDesc(paidMember.getId());
            assertEquals(2, paidMemberRecords.size());
            assertTrue(paidMemberRecords.stream()
                    .allMatch(record -> record.getRecordSource() == MemberRecordSource.BACKFILL_PAYMENT));
            assertTrue(paidMemberRecords.stream().allMatch(record -> record.getPaymentId() != null));
            assertTrue(paidMemberRecords.stream().allMatch(record -> record.getPaymentCompletedAt() != null));

            List<MemberRecord> unpaidMemberRecords =
                    memberRecordRepository.findByMemberIdOrderByYearSemesterDescIdDesc(unpaidMember.getId());
            assertTrue(unpaidMemberRecords.isEmpty());
        }

        @Test
        void 백필은_멱등적으로_동작한다() {
            // given
            Member member = createMember();
            paymentRepository.save(
                    Payment.createForDev(member, PaymentStatus.COMPLETED, YearSemester.YEAR_SEMESTER_2025_1));

            // when
            MemberRecordBackfillResponse first = memberRecordService.backfillFromCompletedPayments();
            MemberRecordBackfillResponse second = memberRecordService.backfillFromCompletedPayments();

            // then 반환값 검증
            assertEquals(1, first.createdRecords());
            assertEquals(0, first.skippedRecords());

            assertEquals(0, second.createdRecords());
            assertEquals(1, second.skippedRecords());

            // then DB 상태 검증
            List<MemberRecord> records =
                    memberRecordRepository.findByMemberIdOrderByYearSemesterDescIdDesc(member.getId());
            assertEquals(1, records.size());
        }
    }

    @Nested
    class 관리자_회원기록_조회 {

        @Test
        void 학기별_회원기록을_페이지로_조회한다() {
            // given
            Member member1 = createMember();
            Member member2 = createMember();

            memberRecordService.createMemberRecordIfAbsent(
                    member1.getId(), YearSemester.YEAR_SEMESTER_2026_1, MemberRecordSource.BACKFILL_PAYMENT);
            memberRecordService.createMemberRecordIfAbsent(
                    member2.getId(), YearSemester.YEAR_SEMESTER_2026_1, MemberRecordSource.BACKFILL_PAYMENT);
            memberRecordService.createMemberRecordIfAbsent(
                    member2.getId(), YearSemester.YEAR_SEMESTER_2025_2, MemberRecordSource.BACKFILL_PAYMENT);

            // when
            AdminMemberRecordPageResponse response =
                    memberRecordService.getMemberRecordsByYearSemester(YearSemester.YEAR_SEMESTER_2026_1, 0, 50);

            // then
            assertEquals(2, response.content().size());
            assertTrue(
                    response.content().stream().anyMatch(item -> item.memberId().equals(member1.getId())));
            assertTrue(
                    response.content().stream().anyMatch(item -> item.memberId().equals(member2.getId())));
            assertTrue(response.content().stream()
                    .allMatch(item -> item.yearSemester() == YearSemester.YEAR_SEMESTER_2026_1));
            assertTrue(response.content().stream()
                    .anyMatch(item -> item.snapshotName().equals(member1.getName())));
            assertTrue(response.content().stream().anyMatch(item -> item.snapshotRole() == Role.USER));
        }

        @Test
        void 회원별_타임라인을_조회한다() {
            // given
            Member member = createMember();

            memberRecordService.createMemberRecordIfAbsent(
                    member.getId(), YearSemester.YEAR_SEMESTER_2025_1, MemberRecordSource.BACKFILL_PAYMENT);
            memberRecordService.createMemberRecordIfAbsent(
                    member.getId(), YearSemester.YEAR_SEMESTER_2026_1, MemberRecordSource.PAYMENT_COMPLETED);

            // when
            List<AdminMemberRecordTimelineResponse> timeline =
                    memberRecordService.getMemberRecordTimeline(member.getId());

            // then
            assertEquals(2, timeline.size());
            assertEquals(YearSemester.YEAR_SEMESTER_2026_1, timeline.getFirst().yearSemester());
            assertEquals(
                    MemberRecordSource.PAYMENT_COMPLETED, timeline.getFirst().recordSource());
            assertEquals(member.getName(), timeline.getFirst().snapshotName());
            assertEquals(member.getEmail(), timeline.getFirst().snapshotEmail());
            assertEquals(member.getRole(), timeline.getFirst().snapshotRole());
        }

        @Test
        void 기록_생성_후_회원정보가_변경되어도_스냅샷은_유지된다() {
            // given
            Member member = createMember();
            memberRecordService.createMemberRecordIfAbsent(
                    member.getId(), YearSemester.YEAR_SEMESTER_2026_1, MemberRecordSource.BACKFILL_PAYMENT);

            String oldStudentId = member.getStudentId();
            String oldName = member.getName();
            String oldEmail = member.getEmail();

            member.updateName("변경된이름");
            member.updateEmail("changed@dankook.ac.kr");
            member.updatePersonalInfo(
                    "010-0000-9999", "32999999", Department.퇴계혁신칼리지_SW융합계열광역, Grade.ONE, "020202", Gender.FEMALE);
            memberRepository.save(member);

            // when
            List<AdminMemberRecordTimelineResponse> timeline =
                    memberRecordService.getMemberRecordTimeline(member.getId());

            // then
            assertEquals(1, timeline.size());
            assertEquals(oldStudentId, timeline.getFirst().snapshotStudentId());
            assertEquals(oldName, timeline.getFirst().snapshotName());
            assertEquals(oldEmail, timeline.getFirst().snapshotEmail());
        }

        @Test
        void 존재하지_않는_회원의_타임라인_조회는_실패한다() {
            // given
            Long notExistingMemberId = 99_999_999L;

            // when-then
            CustomException exception = assertThrows(
                    CustomException.class, () -> memberRecordService.getMemberRecordTimeline(notExistingMemberId));
            assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
        }
    }
}
