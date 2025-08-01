package aegis.server.domain.study.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.study.domain.*;
import aegis.server.domain.study.dto.request.StudyCreateUpdateRequest;
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
            "10",
            "매주 토요일 2시",
            "Java 기초부터 심화까지",
            "Java에 관심이 있는 분");

    @Nested
    class 스터디_생성 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            // when
            studyService.createStudy(studyCreateRequest, userDetails);

            // then
            Study study = studyRepository.findAll().get(0);
            assertEquals("테스트 스터디", study.getTitle());
            assertEquals(StudyCategory.COMPUTER_SCIENCE, study.getCategory());
            assertEquals(StudyLevel.BASIC, study.getLevel());

            StudyMember studyMember =
                    studyMemberRepository.findByStudyAndMember(study, member).orElse(null);
            assertNotNull(studyMember);
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
            studyService.createStudy(studyCreateRequest, userDetails);
            Study study = studyRepository.findAll().get(0);

            StudyCreateUpdateRequest updateRequest = new StudyCreateUpdateRequest(
                    "수정된 스터디",
                    StudyCategory.WEB,
                    StudyLevel.INTERMEDIATE,
                    "수정된 설명",
                    StudyRecruitmentMethod.APPLICATION,
                    "5",
                    "매주 일요일 3시",
                    "디자인 심화 과정",
                    "디자인 경험자");

            // when
            studyService.updateStudy(study.getId(), updateRequest, userDetails);

            // then
            Study updatedStudy = studyRepository.findById(study.getId()).orElse(null);
            assertNotNull(updatedStudy);
            assertEquals("수정된 스터디", updatedStudy.getTitle());
            assertEquals(StudyCategory.WEB, updatedStudy.getCategory());
            assertEquals(StudyLevel.INTERMEDIATE, updatedStudy.getLevel());
        }

        @Test
        void 존재하지_않는_회원이면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            studyService.createStudy(studyCreateRequest, userDetails);
            Study study = studyRepository.findAll().get(0);

            UserDetails invalidUserDetails = createUserDetails(member);
            ReflectionTestUtils.setField(invalidUserDetails, "memberId", member.getId() + 1L);

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyService.updateStudy(study.getId(), studyCreateRequest, invalidUserDetails));
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

            studyService.createStudy(studyCreateRequest, instructorDetails);
            Study study = studyRepository.findAll().get(0);

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyService.updateStudy(study.getId(), studyCreateRequest, otherUserDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, exception.getErrorCode());
        }
    }
}
