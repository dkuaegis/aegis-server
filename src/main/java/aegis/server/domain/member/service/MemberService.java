package aegis.server.domain.member.service;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.dto.request.MemberUpdateRequest;
import aegis.server.domain.member.dto.response.MemberResponse;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.global.security.dto.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponse getMember(SessionUser sessionUser, Long memberId) {
        if (!Objects.equals(sessionUser.getId(), memberId)) { // session과 memberId값이 같을 경우에만 진행
            throw new IllegalArgumentException("Invalid member id");
        }

        Member member = memberRepository.findById(sessionUser.getId()).orElseThrow();

        return MemberResponse.from(member);
    }

    @Transactional
    public void updateMember(SessionUser sessionUser, MemberUpdateRequest request) {
        Member member = memberRepository.findById(sessionUser.getId()).orElseThrow();

        member.updateMember(
                request.getBirthDate(),
                request.getGender(),
                request.getStudentId(),
                request.getPhoneNumber(),
                request.getDepartment(),
                request.getAcademicStatus(),
                request.getGrade(),
                request.getSemester()
        );
    }
}
