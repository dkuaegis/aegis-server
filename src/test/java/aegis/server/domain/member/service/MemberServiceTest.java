package aegis.server.domain.member.service;

import aegis.server.domain.member.domain.*;
import aegis.server.domain.member.dto.request.MemberUpdateRequest;
import aegis.server.domain.member.dto.response.MemberResponse;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.global.security.dto.SessionUser;
import aegis.server.global.security.oidc.UserAuthInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    private static final String TEST_OIDC_ID = "123456789012345678901";
    private static final String TEST_EMAIL = "test@dankook.ac.kr";
    private static final String TEST_NAME = "홍길동";
    private static final Long TEST_MEMBER_ID = 1L;

    @Mock
    private MemberRepository memberRepository;
    @InjectMocks
    private MemberService memberService;

    private Member createTestMember() {
        Member member = Member.createGuestMember(TEST_OIDC_ID, TEST_EMAIL, TEST_NAME);
        ReflectionTestUtils.setField(member, "id", TEST_MEMBER_ID);
        return member;
    }

    private SessionUser createSessionUser(Member member) {
        UserAuthInfo userAuthInfo = UserAuthInfo.from(member);
        return SessionUser.from(userAuthInfo);
    }

    @Nested
    class 멤버조회 {

        @Test
        void 성공한다() {
            // given
            Member member = createTestMember();
            SessionUser sessionUser = createSessionUser(member);
            when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));

            // when
            MemberResponse memberResponse = memberService.getMember(sessionUser);

            // then
            assertEquals(member.getId(), memberResponse.getId());
            assertEquals(member.getEmail(), memberResponse.getEmail());
            assertEquals(member.getName(), memberResponse.getName());
        }

        @Test
        void 실패한다() {
            // given
            Member member = createTestMember();
            SessionUser sessionUser = createSessionUser(member);
            Long invalidMemberId = member.getId() + 1L;

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                    memberService.getMember(sessionUser));
        }
    }

    @Nested
    class 멤버수정 {

        @Test
        void 성공한다() {
            // given
            Member member = createTestMember();
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

            when(memberRepository.findById(sessionUser.getId())).thenReturn(Optional.of(member));

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
            SessionUser sessionUser = createSessionUser(createTestMember());
            MemberUpdateRequest updateRequest = MemberUpdateRequest.builder().build();
            when(memberRepository.findById(any())).thenThrow(new NoSuchElementException());

            // when & then
            assertThrows(NoSuchElementException.class, () ->
                    memberService.updateMember(sessionUser, updateRequest));
        }
    }
}
