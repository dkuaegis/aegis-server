package aegis.server.global.security.oidc;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Student;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.member.repository.StudentRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final MemberRepository memberRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        Member member = findOrCreateMemberAndStudent(oidcUser);

        return new CustomOidcUser(oidcUser, member);
    }

    private Member findOrCreateMemberAndStudent(OidcUser oidcUser) {
        String oidcId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        if (email == null || !email.endsWith("@dankook.ac.kr")) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Member member = memberRepository.findByOidcId(oidcId).orElseGet(
                () -> memberRepository.save(Member.createMember(oidcId, email, name))
        );

        studentRepository.findByMemberInCurrentYearSemester(member).orElseGet(
                () -> studentRepository.save(Student.from(member))
        );

        return member;
    }
}
