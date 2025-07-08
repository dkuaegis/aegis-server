package aegis.server.domain.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.*;
import aegis.server.domain.member.dto.request.PersonalInfoUpdateRequest;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberServiceTest extends IntegrationTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    private final PersonalInfoUpdateRequest personalInfoUpdateRequest = new PersonalInfoUpdateRequest(
            "010-1234-5678", "32000000", Department.SW융합대학_컴퓨터공학과, Grade.THREE, "010101", Gender.MALE);

    @Nested
    class 개인정보_수정 {

        @Test
        void 성공한다() {
            // given
            Member member = createInitialMember();
            UserDetails userDetails = createUserDetails(member);

            // when
            memberService.updatePersonalInfo(userDetails, personalInfoUpdateRequest);

            // then
            Member updatedMember = memberRepository.findById(member.getId()).get();

            assertEquals(personalInfoUpdateRequest.birthDate(), updatedMember.getBirthdate());
            assertEquals(personalInfoUpdateRequest.gender(), updatedMember.getGender());
            assertEquals(personalInfoUpdateRequest.phoneNumber(), updatedMember.getPhoneNumber());
            assertEquals(personalInfoUpdateRequest.studentId(), updatedMember.getStudentId());
            assertEquals(personalInfoUpdateRequest.department(), updatedMember.getDepartment());
            assertEquals(personalInfoUpdateRequest.grade(), updatedMember.getGrade());
        }

        @Test
        void member를_찾을_수_없다면_실패한다() {
            // given
            Member member = createInitialMember();
            UserDetails userDetails = createUserDetails(member);
            ReflectionTestUtils.setField(userDetails, "memberId", member.getId() + 1L);

            // when-then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> memberService.updatePersonalInfo(userDetails, personalInfoUpdateRequest));
            assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
        }
    }
}
