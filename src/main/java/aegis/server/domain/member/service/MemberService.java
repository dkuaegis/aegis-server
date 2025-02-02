package aegis.server.domain.member.service;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Student;
import aegis.server.domain.member.dto.request.PersonalInfoUpdateRequest;
import aegis.server.domain.member.dto.response.PersonalInfoResponse;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.member.repository.StudentRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final StudentRepository studentRepository;

    public PersonalInfoResponse getPersonalInfo(UserDetails userDetails) {
        Member member = memberRepository.findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Student student = studentRepository.findByMember(member)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        return PersonalInfoResponse.from(member, student);
    }

    @Transactional
    public void updatePersonalInfo(UserDetails userDetails, PersonalInfoUpdateRequest request) {
        Member member = memberRepository.findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Student student = studentRepository.findByMemberInCurrentYearSemester(member)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        member.updateMember(
                request.gender(),
                request.birthDate(),
                request.phoneNumber()
        );
        student.updateStudent(
                request.studentId(),
                request.department(),
                request.academicStatus(),
                request.grade(),
                request.semester()
        );
    }
}
