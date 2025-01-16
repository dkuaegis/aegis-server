package aegis.server.domain.member.service;

import aegis.server.common.IntegrationTest;
import aegis.server.domain.member.domain.*;
import aegis.server.domain.member.dto.request.MemberUpdateRequest;
import aegis.server.domain.member.dto.response.MemberResponse;
import aegis.server.global.security.dto.SessionUser;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MemberServiceTest extends IntegrationTest {

    @Autowired
    private MemberService memberService;

    @Test
    void updateJoniProgress() {
        // given
        Member member = createGuestMember();
        SessionUser sessionUser = createSessionUser(member);

        // when
        MemberResponse memberResponse = memberService.getMember(sessionUser);

        // then
        assertEquals(memberResponse.getJoinProgress(), JoinProgress.PERSONAL_INFORMATION);
    }

    @Nested
    class 멤버수정 {

        @Test
        void 성공한다() {
            // given
            Member member = createGuestMember();
            SessionUser sessionUser = createSessionUser(member);

            MemberUpdateRequest updateRequest = MemberUpdateRequest.builder()
                    .birthDate("010101")
                    .gender(Gender.MALE)
                    .studentId("32000000")
                    .phoneNumber("010-1234-5678")
                    .department(Department.COMPUTER_ENGINEERING)
                    .academicStatus(AcademicStatus.ENROLLED)
                    .grade(Grade.ONE)
                    .semester(Semester.FIRST)
                    .build();

            // when
            memberService.updateMember(sessionUser, updateRequest);

            // then
            assertEquals(updateRequest.getBirthDate(), member.getBirthDate());
            assertEquals(updateRequest.getGender(), member.getGender());
            assertEquals(updateRequest.getStudentId(), member.getStudentId());
            assertEquals(updateRequest.getPhoneNumber(), member.getPhoneNumber());
            assertEquals(updateRequest.getDepartment(), member.getDepartment());
            assertEquals(updateRequest.getAcademicStatus(), member.getAcademicStatus());
            assertEquals(updateRequest.getGrade(), member.getGrade());
            assertEquals(updateRequest.getSemester(), member.getSemester());
        }

        @Test
        void 실패한다() {
            // given
            Member member = createGuestMember();
            SessionUser sessionUser = createSessionUser(member);
            MemberUpdateRequest updateRequest = MemberUpdateRequest.builder().build();

            memberRepository.delete(member); // update가 실패할 경우는 member가 delete되었을때 밖에 없음
            // when & then
            assertThrows(NoSuchElementException.class, () ->
                    memberService.updateMember(sessionUser, updateRequest));
        }
    }
}
