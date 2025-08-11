package aegis.server.domain.study.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.*;
import aegis.server.domain.study.domain.*;
import aegis.server.domain.study.dto.request.StudyCreateUpdateRequest;
import aegis.server.domain.study.dto.response.StudyDetailResponse;
import aegis.server.domain.study.dto.response.StudySummaryResponse;
import aegis.server.domain.study.repository.StudyMemberRepository;
import aegis.server.domain.study.repository.StudyRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

class StudyServiceTest extends IntegrationTest {

    @Autowired
    StudyService studyService;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    StudyMemberRepository studyMemberRepository;

    private final StudyCreateUpdateRequest studyCreateRequest = new StudyCreateUpdateRequest(
            "테스트 스터디",
            StudyCategory.COMPUTER_SCIENCE,
            StudyLevel.BASIC,
            "테스트 스터디 설명",
            StudyRecruitmentMethod.FCFS,
            10,
            "매주 토요일 2시",
            "Java 기초부터 심화까지",
            "Java에 관심이 있는 분");

    private StudyCreateUpdateRequest createStudyRequest(String title) {
        return new StudyCreateUpdateRequest(
                title,
                StudyCategory.WEB,
                StudyLevel.BASIC,
                "테스트 스터디 설명",
                StudyRecruitmentMethod.FCFS,
                5,
                "매주 토요일",
                "테스트 커리큘럼",
                "테스트 대상자");
    }

    private StudyCreateUpdateRequest createUpdateRequest() {
        return new StudyCreateUpdateRequest(
                "수정된 스터디",
                StudyCategory.WEB,
                StudyLevel.INTERMEDIATE,
                "수정된 설명",
                StudyRecruitmentMethod.APPLICATION,
                5,
                "매주 일요일 3시",
                "디자인 심화 과정",
                "디자인 경험자");
    }

    @Nested
    class 스터디_상세_조회 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            studyService.createStudy(studyCreateRequest, userDetails);
            Study study = studyRepository.findAll().getFirst();

            // when
            StudyDetailResponse response = studyService.getStudyDetail(study.getId());

            // then
            assertEquals(study.getId(), response.id());
            assertEquals("테스트 스터디", response.title());
            assertEquals(0, response.participantCount());
            assertEquals(10, response.maxParticipants());
            assertEquals(member.getName(), response.instructor());
        }

        @Test
        void 존재하지_않는_스터디면_실패한다() {
            // when & then
            CustomException exception = assertThrows(CustomException.class, () -> studyService.getStudyDetail(9999L));
            assertEquals(ErrorCode.STUDY_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class 스터디_목록_조회 {

        @Test
        void 성공한다() {
            // given
            Member member1 = createMember();
            Member member2 = createMember();
            UserDetails userDetails1 = createUserDetails(member1);
            UserDetails userDetails2 = createUserDetails(member2);

            StudyCreateUpdateRequest request1 = createStudyRequest("스터디 1");
            StudyCreateUpdateRequest request2 = createStudyRequest("스터디 2");

            studyService.createStudy(request1, userDetails1);
            studyService.createStudy(request2, userDetails2);

            // when
            List<StudySummaryResponse> response = studyService.getStudyList();

            // then
            assertEquals(2, response.size());

            StudySummaryResponse study1 = response.getFirst();
            assertEquals("스터디 1", study1.title());
            assertEquals(0, study1.participantCount());
            assertEquals(member1.getName(), study1.instructor());

            StudySummaryResponse study2 = response.get(1);
            assertEquals("스터디 2", study2.title());
            assertEquals(0, study2.participantCount());
            assertEquals(member2.getName(), study2.instructor());
        }

        @Test
        void 스터디가_없다면_빈_리스트를_반환한다() {
            // when
            List<StudySummaryResponse> response = studyService.getStudyList();

            // then
            assertTrue(response.isEmpty());
        }
    }

    @Nested
    class 스터디_생성 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            // when
            StudyDetailResponse response = studyService.createStudy(studyCreateRequest, userDetails);

            // then
            // 반환값 검증
            assertNotNull(response.id());
            assertEquals("테스트 스터디", response.title());
            assertEquals(StudyCategory.COMPUTER_SCIENCE, response.category());
            assertEquals(StudyLevel.BASIC, response.level());
            assertEquals(member.getName(), response.instructor());
            assertEquals(10, response.maxParticipants());

            // DB 상태 검증
            Study study = studyRepository.findById(response.id()).get();
            assertEquals("테스트 스터디", study.getTitle());
            assertEquals(StudyCategory.COMPUTER_SCIENCE, study.getCategory());
            assertEquals(StudyLevel.BASIC, study.getLevel());

            StudyMember studyMember =
                    studyMemberRepository.findByStudyAndMember(study, member).get();
            assertEquals(StudyRole.INSTRUCTOR, studyMember.getRole());
        }

        @Test
        void 존재하지_않는_회원이면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            ReflectionTestUtils.setField(userDetails, "memberId", member.getId() + 1L);

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class, () -> studyService.createStudy(studyCreateRequest, userDetails));
            assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class 스터디_수정 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            StudyDetailResponse createdStudy = studyService.createStudy(studyCreateRequest, userDetails);

            StudyCreateUpdateRequest updateRequest = createUpdateRequest();

            // when
            StudyDetailResponse response = studyService.updateStudy(createdStudy.id(), updateRequest, userDetails);

            // then
            // 반환값 검증
            assertEquals(createdStudy.id(), response.id());
            assertEquals("수정된 스터디", response.title());
            assertEquals(StudyCategory.WEB, response.category());
            assertEquals(StudyLevel.INTERMEDIATE, response.level());
            assertEquals(member.getName(), response.instructor());
            assertEquals(5, response.maxParticipants());

            // DB 상태 검증
            Study updatedStudy = studyRepository.findById(response.id()).get();
            assertEquals("수정된 스터디", updatedStudy.getTitle());
            assertEquals(StudyCategory.WEB, updatedStudy.getCategory());
            assertEquals(StudyLevel.INTERMEDIATE, updatedStudy.getLevel());
        }

        @Test
        void 존재하지_않는_회원이면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            StudyDetailResponse createdStudy = studyService.createStudy(studyCreateRequest, userDetails);

            UserDetails invalidUserDetails = createUserDetails(member);
            ReflectionTestUtils.setField(invalidUserDetails, "memberId", member.getId() + 1L);

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyService.updateStudy(createdStudy.id(), studyCreateRequest, invalidUserDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, exception.getErrorCode());
        }

        @Test
        void 존재하지_않는_스터디면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class, () -> studyService.updateStudy(9999L, studyCreateRequest, userDetails));
            assertEquals(ErrorCode.STUDY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 스터디장이_아니면_실패한다() {
            // given
            Member instructor = createMember();
            Member otherMember = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            UserDetails otherUserDetails = createUserDetails(otherMember);

            StudyDetailResponse createdStudy = studyService.createStudy(studyCreateRequest, instructorDetails);

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyService.updateStudy(createdStudy.id(), studyCreateRequest, otherUserDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, exception.getErrorCode());
        }
    }
}
