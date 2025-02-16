package aegis.server.global.security.oidc;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Student;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.member.repository.StudentRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${email-restriction.enabled}")
    private boolean emailRestrictionEnabled;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        Member member = findOrCreateMember(oidcUser);
        findOrCreateStudent(member);

        return new CustomOidcUser(oidcUser, member);
    }

    private Member findOrCreateMember(OidcUser oidcUser) {
        String oidcId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        if (email == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        } else if (emailRestrictionEnabled && !email.endsWith("@dankook.ac.kr")) {
            throw new CustomException(ErrorCode.NOT_DKU_EMAIL);
        }


        return memberRepository.findByOidcId(oidcId).orElseGet(
                () -> memberRepository.save(Member.create(oidcId, email, name))
        );
    }

    private void findOrCreateStudent(Member member) {
        studentRepository.findByMemberInCurrentYearSemester(member).orElseGet(
                () -> studentRepository.save(Student.from(member))
        );
    }
}
