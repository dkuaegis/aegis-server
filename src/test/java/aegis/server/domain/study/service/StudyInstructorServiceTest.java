package aegis.server.domain.study.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.study.domain.*;
import aegis.server.domain.study.dto.request.StudyCreateUpdateRequest;
import aegis.server.domain.study.dto.response.AttendanceCodeIssueResponse;
import aegis.server.domain.study.dto.response.AttendanceMatrixResponse;
import aegis.server.domain.study.dto.response.AttendanceMemberRow;
import aegis.server.domain.study.dto.response.GeneralStudyDetail;
import aegis.server.domain.study.dto.response.InstructorStudyApplicationReason;
import aegis.server.domain.study.dto.response.InstructorStudyApplicationSummary;
import aegis.server.domain.study.dto.response.InstructorStudyMemberResponse;
import aegis.server.domain.study.repository.StudyApplicationRepository;
import aegis.server.domain.study.repository.StudyMemberRepository;
import aegis.server.domain.study.repository.StudyRepository;
import aegis.server.domain.study.repository.StudySessionRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

class StudyInstructorServiceTest extends IntegrationTest {

    @Autowired
    StudyInstructorService studyInstructorService;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    StudyMemberRepository studyMemberRepository;

    @Autowired
    StudyApplicationRepository studyApplicationRepository;

    @Autowired
    StudySessionRepository studySessionRepository;

    @Autowired
    aegis.server.domain.study.repository.StudyAttendanceRepository studyAttendanceRepository;

    @MockitoBean
    Clock clock;

    @BeforeEach
    void setupClock() {
        given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
        given(clock.instant()).willReturn(Instant.parse("2025-09-11T01:00:00Z"));
    }

    @Nested
    class 스터디_신청_목록_조회 {

        @Test
        void 강사가_자신의_스터디_신청_목록을_조회할_수_있다() {
            // given
            Member instructor = createMember();
            Member applicant1 = createMember();
            Member applicant2 = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);

            Study study = createStudyWithInstructor(instructor);
            createStudyApplication(study, applicant1, "신청 사유1");
            createStudyApplication(study, applicant2, "신청 사유2");

            // when
            List<InstructorStudyApplicationSummary> response =
                    studyInstructorService.findAllStudyApplications(study.getId(), instructorDetails);

            // then
            assertEquals(2, response.size());
            assertTrue(response.stream().anyMatch(summary -> summary.name().equals(applicant1.getName())));
            assertTrue(response.stream().anyMatch(summary -> summary.name().equals(applicant2.getName())));
        }

        @Test
        void 신청이_없는_경우_빈_리스트를_반환한다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);

            // when
            List<InstructorStudyApplicationSummary> response =
                    studyInstructorService.findAllStudyApplications(study.getId(), instructorDetails);

            // then
            assertEquals(0, response.size());
        }

        @Test
        void 강사가_아닌_사용자가_조회하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            Member nonInstructor = createMember();
            UserDetails nonInstructorDetails = createUserDetails(nonInstructor);
            Study study = createStudyWithInstructor(instructor);

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.findAllStudyApplications(study.getId(), nonInstructorDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, exception.getErrorCode());
        }
    }

    @Nested
    class 출석_코드_발급 {

        @Test
        void 스터디장이_오늘의_세션에_대한_코드를_발급한다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);

            // when
            AttendanceCodeIssueResponse response =
                    studyInstructorService.issueAttendanceCode(study.getId(), instructorDetails);

            // then
            // 반환값 검증
            assertNotNull(response);
            assertNotNull(response.code());
            assertEquals(4, response.code().length());
            assertNotNull(response.sessionId());

            // DB 상태 검증
            StudySession session =
                    studySessionRepository.findById(response.sessionId()).get();
            assertNotNull(session);
            assertEquals(LocalDate.now(clock), session.getSessionDate());
            assertEquals(response.code(), session.getAttendanceCode());
        }

        @Test
        void 같은_날_재발급하면_세션과_코드는_유지된다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);

            AttendanceCodeIssueResponse first =
                    studyInstructorService.issueAttendanceCode(study.getId(), instructorDetails);

            // when
            AttendanceCodeIssueResponse second =
                    studyInstructorService.issueAttendanceCode(study.getId(), instructorDetails);

            // then
            // 반환값 검증
            assertEquals(first.sessionId(), second.sessionId());
            assertEquals(first.code(), second.code());

            // DB 상태 검증
            StudySession session =
                    studySessionRepository.findById(first.sessionId()).get();
            assertEquals(first.code(), session.getAttendanceCode());
            assertEquals(second.code(), session.getAttendanceCode());
        }

        @Test
        void 스터디장이_아니면_발급할_수_없다() {
            // given
            Member instructor = createMember();
            Member notInstructor = createMember();
            UserDetails notInstructorDetails = createUserDetails(notInstructor);
            Study study = createStudyWithInstructor(instructor);

            // when & then
            CustomException e = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.issueAttendanceCode(study.getId(), notInstructorDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, e.getErrorCode());
        }

        @Test
        void 날짜가_바뀌면_새로운_세션이_생성된다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);

            // when: 순차 호출마다 다른 날짜가 되도록 스텁
            given(clock.instant())
                    .willReturn(Instant.parse("2025-09-11T10:00:00Z"), Instant.parse("2025-09-12T10:00:00Z"));

            AttendanceCodeIssueResponse first =
                    studyInstructorService.issueAttendanceCode(study.getId(), instructorDetails);

            AttendanceCodeIssueResponse second =
                    studyInstructorService.issueAttendanceCode(study.getId(), instructorDetails);

            // then
            assertNotEquals(first.sessionId(), second.sessionId());
        }
    }

    @Nested
    class 스터디원_목록_조회 {

        @Test
        void 강사가_자신의_스터디원_목록을_조회할_수_있다() {
            // given
            Member instructor = createMember();
            Member participant1 = createMember();
            Member participant2 = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);

            Study study = createStudyWithInstructor(instructor);

            // 참여자 등록
            studyMemberRepository.save(StudyMember.create(study, participant1, StudyRole.PARTICIPANT));
            studyMemberRepository.save(StudyMember.create(study, participant2, StudyRole.PARTICIPANT));

            // when
            List<InstructorStudyMemberResponse> response =
                    studyInstructorService.findAllStudyMembers(study.getId(), instructorDetails);

            // then
            assertEquals(2, response.size());
            assertTrue(response.stream().anyMatch(r -> r.name().equals(participant1.getName())));
            assertTrue(response.stream().anyMatch(r -> r.name().equals(participant2.getName())));
            assertTrue(response.stream().allMatch(r -> r.phoneNumber() != null));
            assertTrue(response.stream().allMatch(r -> r.studentId() != null));
        }

        @Test
        void 스터디원이_없으면_빈_리스트를_반환한다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);

            // when
            List<InstructorStudyMemberResponse> response =
                    studyInstructorService.findAllStudyMembers(study.getId(), instructorDetails);

            // then
            assertEquals(0, response.size());
        }

        @Test
        void 강사가_아닌_사용자가_조회하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            Member nonInstructor = createMember();
            UserDetails nonInstructorDetails = createUserDetails(nonInstructor);
            Study study = createStudyWithInstructor(instructor);

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.findAllStudyMembers(study.getId(), nonInstructorDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, exception.getErrorCode());
        }
    }

    @Nested
    class 출석_현황_조회 {

        @Test
        void 강사가_출석_매트릭스를_조회할_수_있다() {
            // given
            Member instructor = createMember();
            Member p1 = createMember();
            Member p2 = createMember();
            Member p3 = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);

            Study study = createStudyWithInstructor(instructor);

            // 참여자 등록
            studyMemberRepository.save(StudyMember.create(study, p1, StudyRole.PARTICIPANT));
            studyMemberRepository.save(StudyMember.create(study, p2, StudyRole.PARTICIPANT));
            studyMemberRepository.save(StudyMember.create(study, p3, StudyRole.PARTICIPANT));

            // 세션 2개 생성 (날짜 오름차순)
            StudySession s1 =
                    studySessionRepository.save(StudySession.create(study, LocalDate.of(2025, 9, 10), "1111"));
            StudySession s2 =
                    studySessionRepository.save(StudySession.create(study, LocalDate.of(2025, 9, 17), "2222"));

            // 출석: p1 -> s1, s2 / p2 -> s2 / p3 -> 없음
            studyAttendanceRepository.save(StudyAttendance.create(s1, p1));
            studyAttendanceRepository.save(StudyAttendance.create(s2, p1));
            studyAttendanceRepository.save(StudyAttendance.create(s2, p2));

            // when
            AttendanceMatrixResponse response =
                    studyInstructorService.findAttendanceMatrix(study.getId(), instructorDetails);

            // then
            assertEquals(2, response.sessions().size());
            assertEquals(LocalDate.of(2025, 9, 10), response.sessions().get(0).date());
            assertEquals(LocalDate.of(2025, 9, 17), response.sessions().get(1).date());

            // 멤버별 검증 (memberId 매칭)
            Map<Long, AttendanceMemberRow> byId = response.members().stream()
                    .collect(java.util.stream.Collectors.toMap(AttendanceMemberRow::memberId, m -> m));

            List<Boolean> p1Row = byId.get(p1.getId()).attendance();
            List<Boolean> p2Row = byId.get(p2.getId()).attendance();
            List<Boolean> p3Row = byId.get(p3.getId()).attendance();

            assertEquals(List.of(true, true), p1Row);
            assertEquals(List.of(false, true), p2Row);
            assertEquals(List.of(false, false), p3Row);
        }

        @Test
        void 세션이_없으면_빈_컬럼을_반환한다() {
            // given
            Member instructor = createMember();
            Member p1 = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);
            studyMemberRepository.save(StudyMember.create(study, p1, StudyRole.PARTICIPANT));

            // when
            AttendanceMatrixResponse response =
                    studyInstructorService.findAttendanceMatrix(study.getId(), instructorDetails);

            // then
            assertEquals(0, response.sessions().size());
            assertEquals(1, response.members().size());
            assertEquals(0, response.members().get(0).attendance().size());
        }

        @Test
        void 강사가_아니면_조회할_수_없다() {
            // given
            Member instructor = createMember();
            Member notInstructor = createMember();
            UserDetails notInstructorDetails = createUserDetails(notInstructor);
            Study study = createStudyWithInstructor(instructor);

            // when & then
            CustomException e = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.findAttendanceMatrix(study.getId(), notInstructorDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, e.getErrorCode());
        }

        @Test
        void 참여자가_없으면_members_빈_리스트() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);

            // 세션만 생성
            studySessionRepository.save(StudySession.create(study, LocalDate.of(2025, 9, 3), "1111"));

            // when
            AttendanceMatrixResponse response =
                    studyInstructorService.findAttendanceMatrix(study.getId(), instructorDetails);

            // then
            assertEquals(1, response.sessions().size());
            assertEquals(0, response.members().size());
        }

        @Test
        void 멤버_이름_오름차순으로_정렬된다() {
            // given
            Member instructor = createMember();
            Member a = createMember();
            Member b = createMember();
            Member c = createMember();
            a.updateName("이지수");
            b.updateName("강다연");
            c.updateName("박서준");

            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);

            studyMemberRepository.save(StudyMember.create(study, a, StudyRole.PARTICIPANT));
            studyMemberRepository.save(StudyMember.create(study, b, StudyRole.PARTICIPANT));
            studyMemberRepository.save(StudyMember.create(study, c, StudyRole.PARTICIPANT));

            // 세션 1개(정렬 무관)
            StudySession s1 = studySessionRepository.save(StudySession.create(study, LocalDate.of(2025, 9, 3), "1111"));
            // 출석은 일부만 체크
            studyAttendanceRepository.save(StudyAttendance.create(s1, a));

            // when
            AttendanceMatrixResponse response =
                    studyInstructorService.findAttendanceMatrix(study.getId(), instructorDetails);

            // then: 이름 오름차순: 강다연, 박서준, 이지수
            assertEquals("강다연", response.members().get(0).name());
            assertEquals("박서준", response.members().get(1).name());
            assertEquals("이지수", response.members().get(2).name());
        }

        @Test
        void 참여자_아닌_멤버의_출석은_무시된다() {
            // given
            Member instructor = createMember();
            Member participant = createMember();
            Member outsider = createMember(); // 스터디 참여자가 아님
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);

            studyMemberRepository.save(StudyMember.create(study, participant, StudyRole.PARTICIPANT));

            StudySession s1 = studySessionRepository.save(StudySession.create(study, LocalDate.of(2025, 9, 3), "1111"));

            // 출석: outsider의 출석도 넣어보지만 결과에는 반영되면 안 됨
            studyAttendanceRepository.save(StudyAttendance.create(s1, outsider));

            // when
            AttendanceMatrixResponse response =
                    studyInstructorService.findAttendanceMatrix(study.getId(), instructorDetails);

            // then
            assertEquals(1, response.sessions().size());
            assertEquals(1, response.members().size());
            // 유일한 멤버는 participant여야 함
            assertEquals(participant.getId(), response.members().get(0).memberId());
            // outsider 출석은 무시되므로 false
            assertEquals(java.util.List.of(false), response.members().get(0).attendance());
        }

        @Test
        void 다른_스터디_데이터_혼입되지_않는다() {
            // given: study1, study2 각각 세션/참여자/출석 생성
            Member instructor1 = createMember();
            Member instructor2 = createMember();
            UserDetails instructor1Details = createUserDetails(instructor1);

            Study study1 = createStudyWithInstructor(instructor1);
            Study study2 = createStudyWithInstructor(instructor2);

            Member s1p = createMember();
            Member s2p = createMember();
            studyMemberRepository.save(StudyMember.create(study1, s1p, StudyRole.PARTICIPANT));
            studyMemberRepository.save(StudyMember.create(study2, s2p, StudyRole.PARTICIPANT));

            StudySession s1 =
                    studySessionRepository.save(StudySession.create(study1, LocalDate.of(2025, 9, 3), "1111"));
            StudySession s2 =
                    studySessionRepository.save(StudySession.create(study2, LocalDate.of(2025, 9, 4), "2222"));

            studyAttendanceRepository.save(StudyAttendance.create(s2, s2p)); // study2 출석만 체크

            // when: study1 조회
            AttendanceMatrixResponse response =
                    studyInstructorService.findAttendanceMatrix(study1.getId(), instructor1Details);

            // then: study2 데이터는 포함되면 안 됨
            assertEquals(1, response.sessions().size());
            assertEquals(LocalDate.of(2025, 9, 3), response.sessions().get(0).date());
            assertEquals(1, response.members().size());
            assertEquals(s1p.getId(), response.members().get(0).memberId());
            assertEquals(java.util.List.of(false), response.members().get(0).attendance());
        }

        @Test
        void attendance_배열_길이는_sessions_길이와_같다() {
            // given
            Member instructor = createMember();
            Member p1 = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);

            studyMemberRepository.save(StudyMember.create(study, p1, StudyRole.PARTICIPANT));

            // 세션 3개 생성 (역순으로 insert)
            studySessionRepository.save(StudySession.create(study, LocalDate.of(2025, 9, 17), "3333"));
            studySessionRepository.save(StudySession.create(study, LocalDate.of(2025, 9, 3), "1111"));
            studySessionRepository.save(StudySession.create(study, LocalDate.of(2025, 9, 10), "2222"));

            // when
            AttendanceMatrixResponse response =
                    studyInstructorService.findAttendanceMatrix(study.getId(), instructorDetails);

            // then
            assertEquals(3, response.sessions().size());
            assertEquals(3, response.members().get(0).attendance().size());
        }

        @Test
        void 대량_세션_대량_참여자_패턴_검증() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);

            // 세션 40개 생성 (2025-01-01 부터 연속)
            int sessionCount = 40;
            List<StudySession> sessions = new java.util.ArrayList<>();
            for (int s = 0; s < sessionCount; s++) {
                sessions.add(studySessionRepository.save(
                        StudySession.create(study, LocalDate.of(2025, 1, 1).plusDays(s), String.format("%04d", s))));
            }

            // 멤버 60명 생성 및 등록
            int memberCount = 60;
            List<Member> members = new java.util.ArrayList<>();
            for (int m = 0; m < memberCount; m++) {
                Member mem = createMember();
                members.add(mem);
                studyMemberRepository.save(StudyMember.create(study, mem, StudyRole.PARTICIPANT));
            }

            // 출석 패턴: 세션 s, 멤버 m에 대해 (s % 7 == m % 5)면 출석
            for (int m = 0; m < memberCount; m++) {
                Member mem = members.get(m);
                for (int s = 0; s < sessionCount; s++) {
                    if (s % 7 == m % 5) {
                        studyAttendanceRepository.save(StudyAttendance.create(sessions.get(s), mem));
                    }
                }
            }

            // when
            AttendanceMatrixResponse response =
                    studyInstructorService.findAttendanceMatrix(study.getId(), instructorDetails);

            // then: 크기 검증
            assertEquals(sessionCount, response.sessions().size());
            assertEquals(memberCount, response.members().size());

            // 샘플 멤버 3명에 대해 패턴 검증 (memberId 매핑 사용)
            var byId = response.members().stream()
                    .collect(java.util.stream.Collectors.toMap(AttendanceMemberRow::memberId, m -> m));

            int[] sampleIdx = {0, 13, 47};
            for (int idx : sampleIdx) {
                Member mem = members.get(idx);
                List<Boolean> actual = byId.get(mem.getId()).attendance();
                for (int s = 0; s < sessionCount; s++) {
                    boolean expected = (s % 7 == idx % 5);
                    assertEquals(expected, actual.get(s), "member:" + idx + ", session:" + s);
                }
            }
        }

        @Test
        void 모두_출석하면_모든값_true() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);

            int sessionCount = 30;
            int memberCount = 30;
            List<StudySession> sessions = new java.util.ArrayList<>();
            for (int s = 0; s < sessionCount; s++) {
                sessions.add(studySessionRepository.save(
                        StudySession.create(study, LocalDate.of(2025, 2, 1).plusDays(s), String.format("%04d", s))));
            }
            List<Member> members = new java.util.ArrayList<>();
            for (int m = 0; m < memberCount; m++) {
                Member mem = createMember();
                members.add(mem);
                studyMemberRepository.save(StudyMember.create(study, mem, StudyRole.PARTICIPANT));
            }
            for (Member mem : members) {
                for (StudySession ss : sessions) {
                    studyAttendanceRepository.save(StudyAttendance.create(ss, mem));
                }
            }

            // when
            AttendanceMatrixResponse response =
                    studyInstructorService.findAttendanceMatrix(study.getId(), instructorDetails);

            // then
            assertEquals(sessionCount, response.sessions().size());
            assertEquals(memberCount, response.members().size());
            for (var row : response.members()) {
                assertTrue(row.attendance().stream().allMatch(Boolean::booleanValue));
            }
        }

        @Test
        void 세션0_참여자0_완전빈_결과() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);

            // when
            AttendanceMatrixResponse response =
                    studyInstructorService.findAttendanceMatrix(study.getId(), instructorDetails);

            // then
            assertEquals(0, response.sessions().size());
            assertEquals(0, response.members().size());
        }
    }

    @Nested
    class 스터디_신청_상세_조회 {

        @Test
        void 강사가_자신의_스터디_신청_상세를_조회할_수_있다() {
            // given
            Member instructor = createMember();
            Member applicant = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);

            Study study = createStudyWithInstructor(instructor);
            StudyApplication application = createStudyApplication(study, applicant, "상세 신청 사유입니다");

            // when
            InstructorStudyApplicationReason response = studyInstructorService.findStudyApplicationById(
                    study.getId(), application.getId(), instructorDetails);

            // then
            assertEquals("상세 신청 사유입니다", response.applicationReason());
        }

        @Test
        void 강사가_아닌_사용자가_조회하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            Member applicant = createMember();
            Member nonInstructor = createMember();
            UserDetails nonInstructorDetails = createUserDetails(nonInstructor);

            Study study = createStudyWithInstructor(instructor);
            StudyApplication application = createStudyApplication(study, applicant, "신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.findStudyApplicationById(
                            study.getId(), application.getId(), nonInstructorDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, exception.getErrorCode());
        }

        @Test
        void 존재하지_않는_신청을_조회하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);
            Long nonExistentApplicationId = 999L;

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.findStudyApplicationById(
                            study.getId(), nonExistentApplicationId, instructorDetails));
            assertEquals(ErrorCode.STUDY_APPLICATION_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 다른_스터디의_신청을_조회하면_예외가_발생한다() {
            // given
            Member instructor1 = createMember();
            Member instructor2 = createMember();
            Member applicant = createMember();
            UserDetails instructor1Details = createUserDetails(instructor1);

            Study study1 = createStudyWithInstructor(instructor1);
            Study study2 = createStudyWithInstructor(instructor2);
            StudyApplication application = createStudyApplication(study2, applicant, "신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.findStudyApplicationById(
                            study1.getId(), application.getId(), instructor1Details));
            assertEquals(ErrorCode.STUDY_APPLICATION_ACCESS_DENIED, exception.getErrorCode());
        }
    }

    @Nested
    class 스터디_수정 {

        @Test
        void 강사가_자신의_스터디를_수정할_수_있다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);
            StudyCreateUpdateRequest request = createStudyCreateUpdateRequest();

            // when
            GeneralStudyDetail response = studyInstructorService.updateStudy(study.getId(), request, instructorDetails);

            // then
            // 반환값 검증
            assertEquals(request.title(), response.title());
            assertEquals(request.category(), response.category());
            assertEquals(request.level(), response.level());
            assertEquals(request.description(), response.description());
            assertEquals(request.recruitmentMethod(), response.recruitmentMethod());

            // DB 상태 검증
            Study updatedStudy = studyRepository.findById(study.getId()).get();
            assertEquals(request.title(), updatedStudy.getTitle());
            assertEquals(request.category(), updatedStudy.getCategory());
            assertEquals(request.level(), updatedStudy.getLevel());
        }

        @Test
        void 강사가_아닌_사용자가_수정하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            Member nonInstructor = createMember();
            UserDetails nonInstructorDetails = createUserDetails(nonInstructor);
            Study study = createStudyWithInstructor(instructor);
            StudyCreateUpdateRequest request = createStudyCreateUpdateRequest();

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.updateStudy(study.getId(), request, nonInstructorDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, exception.getErrorCode());
        }

        @Test
        void 존재하지_않는_스터디를_수정하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Long nonExistentStudyId = 999L;
            StudyCreateUpdateRequest request = createStudyCreateUpdateRequest();

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.updateStudy(nonExistentStudyId, request, instructorDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, exception.getErrorCode());
        }
    }

    @Nested
    class 스터디_신청_승인 {

        @Test
        void 강사가_신청을_승인할_수_있다() {
            // given
            Member instructor = createMember();
            Member applicant = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);

            Study study = createStudyWithInstructor(instructor);
            StudyApplication application = createStudyApplication(study, applicant, "신청 사유");

            // when
            studyInstructorService.approveStudyApplication(study.getId(), application.getId(), instructorDetails);

            // then
            // StudyApplication 승인 상태로 변경 검증
            StudyApplication updatedApplication =
                    studyApplicationRepository.findById(application.getId()).get();
            assertEquals(StudyApplicationStatus.APPROVED, updatedApplication.getStatus());

            // StudyMember 생성 검증
            assertTrue(
                    studyMemberRepository.findByStudyAndMember(study, applicant).isPresent());
            StudyMember studyMember =
                    studyMemberRepository.findByStudyAndMember(study, applicant).get();
            assertEquals(StudyRole.PARTICIPANT, studyMember.getRole());

            // currentParticipants 증가 검증
            Study updatedStudy = studyRepository.findById(study.getId()).get();
            assertEquals(1, updatedStudy.getCurrentParticipants());
        }

        @Test
        void 강사가_아닌_사용자가_승인하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            Member applicant = createMember();
            Member nonInstructor = createMember();
            UserDetails nonInstructorDetails = createUserDetails(nonInstructor);

            Study study = createStudyWithInstructor(instructor);
            StudyApplication application = createStudyApplication(study, applicant, "신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.approveStudyApplication(
                            study.getId(), application.getId(), nonInstructorDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, exception.getErrorCode());
        }

        @Test
        void 존재하지_않는_신청을_승인하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);
            Long nonExistentApplicationId = 999L;

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.approveStudyApplication(
                            study.getId(), nonExistentApplicationId, instructorDetails));
            assertEquals(ErrorCode.STUDY_APPLICATION_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 다른_스터디의_신청을_승인하면_예외가_발생한다() {
            // given
            Member instructor1 = createMember();
            Member instructor2 = createMember();
            Member applicant = createMember();
            UserDetails instructor1Details = createUserDetails(instructor1);

            Study study1 = createStudyWithInstructor(instructor1);
            Study study2 = createStudyWithInstructor(instructor2);
            StudyApplication application = createStudyApplication(study2, applicant, "신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.approveStudyApplication(
                            study1.getId(), application.getId(), instructor1Details));
            assertEquals(ErrorCode.STUDY_APPLICATION_ACCESS_DENIED, exception.getErrorCode());
        }

        @Test
        void 정원이_가득_찬_스터디에_승인하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            Member applicant = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);

            Study study = createStudyWithMaxParticipants(instructor, 1);
            study.increaseCurrentParticipant(); // 정원 가득 채움
            StudyApplication application = createStudyApplication(study, applicant, "신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.approveStudyApplication(
                            study.getId(), application.getId(), instructorDetails));
            assertEquals(ErrorCode.STUDY_FULL, exception.getErrorCode());
        }

        @Test
        void 정원이_무제한인_스터디는_승인을_무제한으로_허용한다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);

            int approveCount = 4;
            Study study = createStudyWithMaxParticipants(instructor, 0);

            // 신청서 생성
            Member[] applicants = new Member[approveCount];
            for (int i = 0; i < approveCount; i++) {
                applicants[i] = createMember();
                createStudyApplication(study, applicants[i], "무제한 승인 테스트" + i);
            }

            // when
            List<StudyApplication> applications = studyApplicationRepository.findAllByStudyIdWithMember(study.getId());
            applications.forEach(app ->
                    studyInstructorService.approveStudyApplication(study.getId(), app.getId(), instructorDetails));

            // then
            Study updated = studyRepository.findById(study.getId()).get();
            assertEquals(approveCount, updated.getCurrentParticipants());
        }
    }

    @Nested
    class 스터디_신청_거절 {

        @Test
        void 강사가_신청을_거절할_수_있다() {
            // given
            Member instructor = createMember();
            Member applicant = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);

            Study study = createStudyWithInstructor(instructor);
            StudyApplication application = createStudyApplication(study, applicant, "신청 사유");

            // when
            studyInstructorService.rejectStudyApplication(study.getId(), application.getId(), instructorDetails);

            // then
            // StudyApplication 거절 상태로 변경 검증
            StudyApplication updatedApplication =
                    studyApplicationRepository.findById(application.getId()).get();
            assertEquals(StudyApplicationStatus.REJECTED, updatedApplication.getStatus());
        }

        @Test
        void 강사가_아닌_사용자가_거절하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            Member applicant = createMember();
            Member nonInstructor = createMember();
            UserDetails nonInstructorDetails = createUserDetails(nonInstructor);

            Study study = createStudyWithInstructor(instructor);
            StudyApplication application = createStudyApplication(study, applicant, "신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.rejectStudyApplication(
                            study.getId(), application.getId(), nonInstructorDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, exception.getErrorCode());
        }

        @Test
        void 존재하지_않는_신청을_거절하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);
            Long nonExistentApplicationId = 999L;

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.rejectStudyApplication(
                            study.getId(), nonExistentApplicationId, instructorDetails));
            assertEquals(ErrorCode.STUDY_APPLICATION_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 다른_스터디의_신청을_거절하면_예외가_발생한다() {
            // given
            Member instructor1 = createMember();
            Member instructor2 = createMember();
            Member applicant = createMember();
            UserDetails instructor1Details = createUserDetails(instructor1);

            Study study1 = createStudyWithInstructor(instructor1);
            Study study2 = createStudyWithInstructor(instructor2);
            StudyApplication application = createStudyApplication(study2, applicant, "신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.rejectStudyApplication(
                            study1.getId(), application.getId(), instructor1Details));
            assertEquals(ErrorCode.STUDY_APPLICATION_ACCESS_DENIED, exception.getErrorCode());
        }
    }

    private Study createStudyWithInstructor(Member instructor) {
        Study study = Study.create(
                "강사 스터디",
                StudyCategory.WEB,
                StudyLevel.INTERMEDIATE,
                "강사가 있는 스터디 설명",
                StudyRecruitmentMethod.APPLICATION,
                10,
                "주 2회",
                List.of("테스트 커리큘럼 1", "테스트 커리큘럼 2"),
                List.of("테스트 자격 요건 1", "테스트 자격 요건 2"));
        study = studyRepository.save(study);

        StudyMember studyMember = StudyMember.create(study, instructor, StudyRole.INSTRUCTOR);
        studyMemberRepository.save(studyMember);

        return study;
    }

    private Study createStudyWithMaxParticipants(Member instructor, int maxParticipants) {
        Study study = Study.create(
                "정원 제한 스터디",
                StudyCategory.WEB,
                StudyLevel.INTERMEDIATE,
                "정원 제한 스터디 설명",
                StudyRecruitmentMethod.APPLICATION,
                maxParticipants,
                "주 2회",
                List.of("테스트 커리큘럼 1", "테스트 커리큘럼 2"),
                List.of("테스트 자격 요건 1", "테스트 자격 요건 2"));
        study = studyRepository.save(study);

        StudyMember studyMember = StudyMember.create(study, instructor, StudyRole.INSTRUCTOR);
        studyMemberRepository.save(studyMember);

        return study;
    }

    private StudyApplication createStudyApplication(Study study, Member member, String applicationReason) {
        StudyApplication studyApplication = StudyApplication.create(study, member, applicationReason);
        return studyApplicationRepository.save(studyApplication);
    }

    private StudyCreateUpdateRequest createStudyCreateUpdateRequest() {
        return new StudyCreateUpdateRequest(
                "수정된 스터디",
                StudyCategory.ARTIFICIAL_INTELLIGENCE,
                StudyLevel.ADVANCED,
                "수정된 스터디 설명",
                StudyRecruitmentMethod.FCFS,
                20,
                "주 3회",
                List.of("수정된 커리큘럼 1", "수정된 커리큘럼 2"),
                List.of("수정된 자격 요건 1", "수정된 자격 요건 2"));
    }
}
