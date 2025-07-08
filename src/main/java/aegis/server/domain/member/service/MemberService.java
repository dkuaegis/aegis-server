package aegis.server.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.dto.request.PersonalInfoUpdateRequest;
import aegis.server.domain.member.dto.response.PersonalInfoResponse;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public PersonalInfoResponse getPersonalInfo(UserDetails userDetails) {
        Member member = memberRepository
                .findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return PersonalInfoResponse.from(member);
    }

    @Transactional
    public void updatePersonalInfo(UserDetails userDetails, PersonalInfoUpdateRequest request) {
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
    }
}
