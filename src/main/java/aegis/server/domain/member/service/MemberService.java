package aegis.server.domain.member.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Role;
import aegis.server.domain.member.dto.request.PersonalInfoUpdateRequest;
import aegis.server.domain.member.dto.request.ProfileIconUpdateRequest;
import aegis.server.domain.member.dto.response.MemberDemoteResponse;
import aegis.server.domain.member.dto.response.PersonalInfoResponse;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    public PersonalInfoResponse getPersonalInfo(UserDetails userDetails) {
        Member member = memberRepository
                .findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return PersonalInfoResponse.from(member);
    }

    @Transactional
    public PersonalInfoResponse updatePersonalInfo(UserDetails userDetails, PersonalInfoUpdateRequest request) {
        Member member = memberRepository
                .findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        member.updatePersonalInfo(
                request.phoneNumber(),
                request.studentId(),
                request.department(),
                request.grade(),
                request.birthDate(),
                request.gender());

        return PersonalInfoResponse.from(member);
    }

    @Transactional
    public PersonalInfoResponse updateProfileIcon(UserDetails userDetails, ProfileIconUpdateRequest request) {
        Member member = memberRepository
                .findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateProfileIcon(request.profileIcon());

        return PersonalInfoResponse.from(member);
    }

    @Transactional
    public MemberDemoteResponse demoteMembersForCurrentSemester() {
        List<Payment> completedPayments =
                paymentRepository.findAllByStatusAndYearSemester(PaymentStatus.COMPLETED, CURRENT_YEAR_SEMESTER);

        List<Long> paidMemberIds = completedPayments.stream()
                .map(payment -> payment.getMember().getId())
                .toList();

        List<Member> unpaidMembers = memberRepository.findAllByRoleNotAndIdNotIn(Role.ADMIN, paidMemberIds);

        List<String> demotedMemberStudentIds =
                unpaidMembers.stream().map(Member::getStudentId).toList();

        unpaidMembers.forEach(Member::demoteToGuest);

        return MemberDemoteResponse.of(demotedMemberStudentIds);
    }
}
