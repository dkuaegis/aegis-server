package aegis.server.global.security.oidc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Student;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.member.repository.StudentRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

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
            throw new OAuth2AuthenticationException(new OAuth2Error("NOT_DKU_EMAIL"));
        }

        Member member = memberRepository.findByOidcId(oidcId).orElse(null);
        if (member == null) {
            return memberRepository.save(Member.create(oidcId, email, name));
        } else {
            if (!member.getEmail().equals(email)) {
                member.updateEmail(email);
            }
            if (!member.getName().equals(name)) {
                member.updateName(name);
            }
        }

        return member;
    }

    private void findOrCreateStudent(Member member) {
        studentRepository.findByMemberInCurrentYearSemester(member).orElseGet(
                () -> studentRepository.save(Student.from(member))
        );
    }
}
