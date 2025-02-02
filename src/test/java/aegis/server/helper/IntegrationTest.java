package aegis.server.helper;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Student;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.member.repository.StudentRepository;
import aegis.server.global.security.oidc.UserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
public class IntegrationTest {

    @Autowired
    DatabaseCleaner databaseCleaner;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
    }

    protected Member createMember() {
        Member member = Member.create("12345678901234567890", "test@dankook.ac.kr", "테스트사용자이름");
        memberRepository.save(member);

        // 한 개의 테스트 케이스에서 여러 번 호출되도 Member 엔티티의 고유성을 위하여 Reflection을 사용하여 수정
        ReflectionTestUtils.setField(member, "oidcId", member.getOidcId() + member.getId());
        ReflectionTestUtils.setField(member, "email", "test" + member.getId() + "@dankook.ac.kr");
        ReflectionTestUtils.setField(member, "name", "테스트사용자이름" + member.getId());

        return memberRepository.save(member);
    }

    protected Student createStudent(Member member) {
        Student student = Student.from(member);
        return studentRepository.save(student);
    }

    protected UserDetails createUserDetails(Member member) {
        return UserDetails.from(member);
    }
}
