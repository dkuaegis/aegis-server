package aegis.server.common;

import aegis.server.domain.member.domain.*;
import aegis.server.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTest {

    @Autowired
    protected DataBaseCleaner dataBaseCleaner;

    @Autowired
    protected MemberRepository memberRepository;

    @BeforeEach
    void cleanDatabase() {
        dataBaseCleaner.clean();
    }

    protected Member createGuestMember() {
        Member member = Member.createGuestMember("test@dankook.ac.kr", "테스트");
        memberRepository.save(member);
        ReflectionTestUtils.setField(member, "email", "test" + member.getId() + "@dankook.ac.kr");
        ReflectionTestUtils.setField(member, "name", "테스트" + member.getId());

        return memberRepository.save(member);
    }

    protected Member createMember() {
        Member member = createGuestMember();
        member.updateMember(
                "010101",
                Gender.MALE,
                "32000000",
                "010-1234-5678",
                Department.COMPUTER_ENGINEERING,
                AcademicStatus.ENROLLED,
                Grade.ONE,
                Semester.FIRST
        );

        return memberRepository.save(member);
    }
}
