package aegis.server.domain.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.*;
import aegis.server.domain.member.dto.request.PersonalInfoUpdateRequest;
import aegis.server.domain.member.dto.request.ProfileIconUpdateRequest;
import aegis.server.domain.member.dto.response.MemberDemoteResponse;
import aegis.server.domain.member.dto.response.PersonalInfoResponse;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberServiceTest extends IntegrationTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Nested
    class 개인정보_조회 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            // when
            PersonalInfoResponse response = memberService.getPersonalInfo(userDetails);

            // then
            assertEquals(member.getName(), response.name());
            assertEquals(member.getStudentId(), response.studentId());
            assertEquals(member.getDepartment(), response.department());
            assertEquals(member.getGrade(), response.grade());
            assertEquals(member.getProfileIcon(), response.profileIcon());
            assertEquals(member.getRole(), response.role());
        }

        @Test
        void member를_찾을_수_없다면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            ReflectionTestUtils.setField(userDetails, "memberId", member.getId() + 1L);

            // when-then
            CustomException exception =
                    assertThrows(CustomException.class, () -> memberService.getPersonalInfo(userDetails));
            assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class 개인정보_수정 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            // when
            PersonalInfoUpdateRequest personalInfoUpdateRequest = new PersonalInfoUpdateRequest(
                    "010-8765-4321", "32000002", Department.퇴계혁신칼리지_SW융합계열광역, Grade.ONE, "020202", Gender.FEMALE);
            PersonalInfoResponse response = memberService.updatePersonalInfo(userDetails, personalInfoUpdateRequest);

            // then
            // 반환값 검증
            assertEquals(member.getName(), response.name());
            assertEquals(personalInfoUpdateRequest.phoneNumber(), response.phoneNumber());
            assertEquals(personalInfoUpdateRequest.studentId(), response.studentId());
            assertEquals(personalInfoUpdateRequest.department(), response.department());
            assertEquals(personalInfoUpdateRequest.grade(), response.grade());
            assertEquals(personalInfoUpdateRequest.birthDate(), response.birthDate());
            assertEquals(personalInfoUpdateRequest.gender(), response.gender());
            assertEquals(member.getRole(), response.role());

            // DB 상태 검증
            Member updatedMember = memberRepository.findById(member.getId()).get();
            assertEquals(personalInfoUpdateRequest.phoneNumber(), updatedMember.getPhoneNumber());
            assertEquals(personalInfoUpdateRequest.studentId(), updatedMember.getStudentId());
            assertEquals(personalInfoUpdateRequest.department(), updatedMember.getDepartment());
            assertEquals(personalInfoUpdateRequest.grade(), updatedMember.getGrade());
            assertEquals(personalInfoUpdateRequest.birthDate(), updatedMember.getBirthdate());
            assertEquals(personalInfoUpdateRequest.gender(), updatedMember.getGender());
        }

        @Test
        void member를_찾을_수_없다면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            ReflectionTestUtils.setField(userDetails, "memberId", member.getId() + 1L);

            // when-then
            PersonalInfoUpdateRequest personalInfoUpdateRequest = new PersonalInfoUpdateRequest(
                    "010-8765-4321", "32000002", Department.퇴계혁신칼리지_SW융합계열광역, Grade.ONE, "020202", Gender.FEMALE);
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> memberService.updatePersonalInfo(userDetails, personalInfoUpdateRequest));
            assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class 프로필_아이콘_수정 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            // when
            ProfileIconUpdateRequest profileIconUpdateRequest = new ProfileIconUpdateRequest(ProfileIcon.JAVA);
            PersonalInfoResponse response = memberService.updateProfileIcon(userDetails, profileIconUpdateRequest);

            // then
            // 반환값 검증
            assertEquals(member.getName(), response.name());
            assertEquals(member.getStudentId(), response.studentId());
            assertEquals(member.getDepartment(), response.department());
            assertEquals(profileIconUpdateRequest.profileIcon(), response.profileIcon());
            assertEquals(member.getRole(), response.role());

            // DB 상태 검증
            Member updatedMember = memberRepository.findById(member.getId()).get();
            assertEquals(profileIconUpdateRequest.profileIcon(), updatedMember.getProfileIcon());
        }

        @Test
        void member를_찾을_수_없다면_실패한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            ReflectionTestUtils.setField(userDetails, "memberId", member.getId() + 1L);

            // when-then
            ProfileIconUpdateRequest profileIconUpdateRequest = new ProfileIconUpdateRequest(ProfileIcon.JAVA);
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> memberService.updateProfileIcon(userDetails, profileIconUpdateRequest));
            assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class 회원_강등 {

        @Test
        void 현재_학기_미납_회원을_강등한다() {
            // given
            Member userMember = createMember();
            userMember.promoteToUser();
            memberRepository.save(userMember);

            // when
            MemberDemoteResponse response = memberService.demoteMembersForCurrentSemester();

            // then
            Member updatedMember = memberRepository.findById(userMember.getId()).get();
            assertTrue(updatedMember.isGuest());
            assertTrue(response.demotedMemberStudentIds().contains(userMember.getStudentId()));
        }

        @Test
        void ADMIN_역할은_강등하지_않는다() {
            // given
            Member adminMember = createMember();
            ReflectionTestUtils.setField(adminMember, "role", Role.ADMIN);
            memberRepository.save(adminMember);

            // when
            MemberDemoteResponse response = memberService.demoteMembersForCurrentSemester();

            // then
            Member updatedMember =
                    memberRepository.findById(adminMember.getId()).get();
            assertEquals(Role.ADMIN, updatedMember.getRole());
            assertTrue(response.demotedMemberStudentIds().isEmpty());
        }

        @Test
        void 결제_완료된_회원은_강등하지_않는다() {
            // given
            Member userMember = createMember();
            userMember.promoteToUser();
            memberRepository.save(userMember);

            Payment payment = Payment.of(userMember);
            payment.completePayment();
            paymentRepository.save(payment);

            // when
            MemberDemoteResponse response = memberService.demoteMembersForCurrentSemester();

            // then
            Member updatedMember = memberRepository.findById(userMember.getId()).get();
            assertEquals(Role.USER, updatedMember.getRole());
            assertTrue(response.demotedMemberStudentIds().isEmpty());
        }

        @Test
        void 강등된_회원의_학번_목록을_반환한다() {
            // given
            Member userMember1 = createMember();
            Member userMember2 = createMember();
            userMember1.promoteToUser();
            userMember2.promoteToUser();
            memberRepository.save(userMember1);
            memberRepository.save(userMember2);

            // when
            MemberDemoteResponse response = memberService.demoteMembersForCurrentSemester();

            // then
            assertEquals(2, response.demotedMemberStudentIds().size());
            assertTrue(response.demotedMemberStudentIds().contains(userMember1.getStudentId()));
            assertTrue(response.demotedMemberStudentIds().contains(userMember2.getStudentId()));
        }
    }
}
