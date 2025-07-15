package aegis.server.domain.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.*;
import aegis.server.domain.member.dto.request.PersonalInfoUpdateRequest;
import aegis.server.domain.member.dto.response.MemberDemoteResponse;
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
