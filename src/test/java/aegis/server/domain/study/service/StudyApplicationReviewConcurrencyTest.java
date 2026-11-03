package aegis.server.domain.study.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.study.domain.Study;
import aegis.server.domain.study.domain.StudyApplication;
import aegis.server.domain.study.domain.StudyApplicationStatus;
import aegis.server.domain.study.domain.StudyCategory;
import aegis.server.domain.study.domain.StudyLevel;
import aegis.server.domain.study.domain.StudyMember;
import aegis.server.domain.study.domain.StudyRecruitmentMethod;
import aegis.server.domain.study.domain.StudyRole;
import aegis.server.domain.study.dto.request.StudyCreateUpdateRequest;
import aegis.server.domain.study.dto.request.StudyEnrollRequest;
import aegis.server.domain.study.repository.StudyApplicationRepository;
import aegis.server.domain.study.repository.StudyMemberRepository;
import aegis.server.domain.study.repository.StudyRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTestWithoutTransactional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("concurrency")
class StudyApplicationReviewConcurrencyTest extends IntegrationTestWithoutTransactional {

    @Autowired
    StudyInstructorService studyInstructorService;

    @Autowired
    StudyGeneralService studyGeneralService;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    StudyApplicationRepository studyApplicationRepository;

    @Autowired
    StudyMemberRepository studyMemberRepository;

    @Test
    void 동시_승인에도_정원을_넘지_않고_참여자_수가_일치한다() throws Exception {
        // given
        int capacity = 5;
        int applicantCount = 24;
        Member instructor = createMember();
        UserDetails instructorDetails = createUserDetails(instructor);
        Study study = createStudy(instructor, StudyRecruitmentMethod.APPLICATION, capacity);
        List<StudyApplication> applications = new ArrayList<>();
        for (int i = 0; i < applicantCount; i++) {
            applications.add(studyApplicationRepository.save(StudyApplication.create(study, createMember(), "지원 사유")));
        }

        // when
        List<Callable<ErrorCode>> tasks = applications.stream()
                .<Callable<ErrorCode>>map(
                        application -> () -> errorCodeOf(() -> studyInstructorService.approveStudyApplication(
                                study.getId(), application.getId(), instructorDetails)))
                .toList();
        List<ErrorCode> results = runConcurrently(tasks);

        // then
        assertEquals(capacity, results.stream().filter(code -> code == null).count());
        assertEquals(
                applicantCount - capacity,
                results.stream().filter(code -> code == ErrorCode.STUDY_FULL).count());
        assertEquals(capacity, studyRepository.findById(study.getId()).get().getCurrentParticipants());
        assertEquals(capacity, participantsOf(study));
    }

    @Test
    void 같은_신청서를_동시에_승인하고_거절해도_상태와_참여자가_일치한다() throws Exception {
        for (int round = 0; round < 10; round++) {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudy(instructor, StudyRecruitmentMethod.APPLICATION, 5);
            StudyApplication application =
                    studyApplicationRepository.save(StudyApplication.create(study, createMember(), "지원 사유"));

            // when
            List<ErrorCode> results = runConcurrently(List.of(
                    () -> errorCodeOf(() -> studyInstructorService.approveStudyApplication(
                            study.getId(), application.getId(), instructorDetails)),
                    () -> errorCodeOf(() -> studyInstructorService.rejectStudyApplication(
                            study.getId(), application.getId(), instructorDetails))));

            // then
            // 거절 후 승인은 허용되지만 승인 후 거절은 실패해야 하므로, 결과는 항상 승인 상태입니다.
            assertNull(results.get(0));
            assertTrue(results.get(1) == null || results.get(1) == ErrorCode.STUDY_APPLICATION_ALREADY_APPROVED);
            StudyApplication reviewed =
                    studyApplicationRepository.findById(application.getId()).orElseThrow();
            assertEquals(StudyApplicationStatus.APPROVED, reviewed.getStatus());
            assertEquals(1, studyRepository.findById(study.getId()).get().getCurrentParticipants());
            assertEquals(1, participantsOf(study));
        }
    }

    @Test
    void 스터디_수정과_동시에_신청해도_참여자_수가_유실되지_않는다() throws Exception {
        // given
        int applicantCount = 30;
        Member instructor = createMember();
        UserDetails instructorDetails = createUserDetails(instructor);
        Study study = createStudy(instructor, StudyRecruitmentMethod.FCFS, 0);
        List<UserDetails> applicants = new ArrayList<>();
        for (int i = 0; i < applicantCount; i++) {
            applicants.add(createUserDetails(createMember()));
        }
        StudyCreateUpdateRequest update = new StudyCreateUpdateRequest(
                "수정된 제목",
                StudyCategory.WEB,
                StudyLevel.BASIC,
                "수정된 설명",
                StudyRecruitmentMethod.FCFS,
                0,
                "주 1회",
                List.of("커리큘럼"),
                List.of("자격 요건"));

        // when
        List<Callable<ErrorCode>> tasks = new ArrayList<>();
        for (UserDetails applicant : applicants) {
            tasks.add(() -> errorCodeOf(
                    () -> studyGeneralService.enrollInStudy(study.getId(), new StudyEnrollRequest(null), applicant)));
            tasks.add(() ->
                    errorCodeOf(() -> studyInstructorService.updateStudy(study.getId(), update, instructorDetails)));
        }
        List<ErrorCode> results = runConcurrently(tasks);

        // then
        assertTrue(results.stream().allMatch(code -> code == null), results.toString());
        assertEquals(
                applicantCount, studyRepository.findById(study.getId()).get().getCurrentParticipants());
        assertEquals(applicantCount, participantsOf(study));
    }

    private Study createStudy(Member instructor, StudyRecruitmentMethod method, int capacity) {
        Study study = studyRepository.save(Study.create(
                "동시성 스터디",
                StudyCategory.WEB,
                StudyLevel.BASIC,
                "설명",
                method,
                capacity,
                "주 1회",
                List.of("커리큘럼"),
                List.of("자격 요건")));
        studyMemberRepository.save(StudyMember.create(study, instructor, StudyRole.INSTRUCTOR));
        return study;
    }

    private long participantsOf(Study study) {
        return studyMemberRepository.findByStudy(study).stream()
                .filter(member -> member.getRole() == StudyRole.PARTICIPANT)
                .count();
    }

    private ErrorCode errorCodeOf(Runnable action) {
        try {
            action.run();
            return null;
        } catch (CustomException e) {
            return e.getErrorCode();
        }
    }

    private List<ErrorCode> runConcurrently(List<Callable<ErrorCode>> tasks) throws Exception {
        try (ExecutorService executor = Executors.newFixedThreadPool(tasks.size())) {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<ErrorCode>> futures = tasks.stream()
                    .map(task -> executor.submit(() -> {
                        start.await();
                        return task.call();
                    }))
                    .toList();
            start.countDown();

            List<ErrorCode> results = new ArrayList<>();
            for (Future<ErrorCode> future : futures) {
                results.add(future.get());
            }
            return results;
        }
    }
}
