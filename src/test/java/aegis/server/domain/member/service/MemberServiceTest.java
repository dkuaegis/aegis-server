package aegis.server.domain.member.service;

import aegis.server.domain.member.domain.*;
import aegis.server.domain.member.dto.request.MemberUpdateRequest;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.global.security.dto.SessionUser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @InjectMocks
    private MemberService memberService;


    @Test
    @DisplayName("Member 불러오기")
    void getMemberTest() {
        //given
        Member member = Member.builder()
                .email("32202469@dankook.ac.kr")
                .name("심재훈")
                .build();

        SessionUser sessionUser = new SessionUser(member.getId(), member.getName(), member.getEmail());
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        //when
        memberService.getMember(sessionUser, member.getId());

        //then
        Assertions.assertThat(sessionUser.getId()).isEqualTo(member.getId());
        Assertions.assertThat(sessionUser.getName()).isEqualTo(member.getName());
        Assertions.assertThat(sessionUser.getEmail()).isEqualTo(member.getEmail());
    }

    @Test
    @DisplayName("Member 불러오기 오류")
    public void getMemberErrorTest() {
        //given
        Member member1 = Member.builder()
                .email("32202469@dankook.ac.kr")
                .name("심재훈")
                .build();
        Member member2 = Member.builder()
                .email("32202293@dankook.ac.kr")
                .name("김장정")
                .build();
        ReflectionTestUtils.setField(member1, "id", 1L);
        ReflectionTestUtils.setField(member2, "id", 2L);
        SessionUser sessionUser = new SessionUser(member1.getId(), member1.getName(), member1.getEmail());

        //when, then
        assertThrows(IllegalArgumentException.class, () -> memberService.getMember(sessionUser, member2.getId()));
    }

    @Test
    @DisplayName("findById 테스트")
    void findByIdTest() {
        Member member = Member.builder()
                .email("32202469@dankook.ac.kr")
                .name("심재훈")
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        SessionUser sessionUser = new SessionUser(member.getId(), member.getName(), member.getEmail());
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        //when
        Member member1 = memberRepository.findById(member.getId()).orElse(null);

        //then
        Assertions.assertThat(member1.getId()).isEqualTo(member.getId());
        Assertions.assertThat(member1.getName()).isEqualTo(member.getName());
        Assertions.assertThat(member1.getEmail()).isEqualTo(member.getEmail());

    }

    @Test
    @DisplayName("updateMember시 잘 저장되어있나 확인")
    void updateMemberTest() throws Exception {
        MemberUpdateRequest dto = MemberUpdateRequest.builder()
                .birthDate("010828")
                .gender(Gender.MALE)
                .studentId("32202469")
                .phoneNumber("010-1234-4312")
                .department(Department.CYBER_SECURITY)
                .academicStatus(AcademicStatus.ENROLLED)
                .grade(Grade.THREE)
                .semester(Semester.FIRST)
                .build();

        Member member = Member.builder()
                .email("32202469@dankook.ac.kr")
                .name("심재훈")
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        SessionUser sessionUser = new SessionUser(member.getId(), member.getName(), member.getEmail());
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));

        //when
        memberService.updateMember(sessionUser, dto);
        Member member1 = memberRepository.findById(member.getId()).orElse(null);

        //then
        Assertions.assertThat(member1.getStudentId()).isEqualTo(dto.getStudentId());
        Assertions.assertThat(member1.getGender()).isEqualTo(dto.getGender());
        Assertions.assertThat(member1.getPhoneNumber()).isEqualTo(dto.getPhoneNumber());
        Assertions.assertThat(member1.getDepartment()).isEqualTo(dto.getDepartment());
        Assertions.assertThat(member1.getAcademicStatus()).isEqualTo(dto.getAcademicStatus());
        Assertions.assertThat(member1.getGrade()).isEqualTo(dto.getGrade());
        Assertions.assertThat(member1.getSemester()).isEqualTo(dto.getSemester());
        Assertions.assertThat(member1.getName()).isEqualTo(member.getName());
        Assertions.assertThat(member1.getEmail()).isEqualTo(member.getEmail());

    }
}