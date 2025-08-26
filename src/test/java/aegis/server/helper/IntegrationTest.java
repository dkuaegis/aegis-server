package aegis.server.helper;

import java.math.BigDecimal;

import net.dv8tion.jda.api.JDA;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import aegis.server.domain.discord.service.listener.DiscordEventListener;
import aegis.server.domain.member.domain.*;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.domain.PointTransactionType;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.domain.point.repository.PointTransactionRepository;
import aegis.server.global.security.oidc.UserDetails;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class IntegrationTest {

    @MockitoBean
    JDA jda;

    @MockitoBean
    DiscordEventListener discordEventListener;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PointAccountRepository pointAccountRepository;

    @Autowired
    PointTransactionRepository pointTransactionRepository;

    protected Member createMember() {
        String uniqueId = String.valueOf(System.nanoTime());
        Member member = Member.create(uniqueId, "test" + uniqueId + "@dankook.ac.kr", "테스트사용자이름" + uniqueId);
        member.updatePersonalInfo(
                "010-1234-5678", "32000001", Department.SW융합대학_컴퓨터공학과, Grade.THREE, "010101", Gender.MALE);
        member.promoteToUser();

        return memberRepository.save(member);
    }

    protected UserDetails createUserDetails(Member member) {
        return UserDetails.from(member);
    }

    protected PointAccount createPointAccount(Member member) {
        return pointAccountRepository.save(PointAccount.create(member));
    }

    protected void createPointTransaction(
            PointAccount account, PointTransactionType type, BigDecimal amount, String reason) {
        pointTransactionRepository.save(PointTransaction.create(account, type, amount, reason, null));
    }

    protected void createEarnPointTransaction(PointAccount account, BigDecimal amount, String reason) {
        account.add(amount);
        pointAccountRepository.save(account);
        createPointTransaction(account, PointTransactionType.EARN, amount, reason);
    }

    protected void createSpendPointTransaction(PointAccount account, BigDecimal amount, String reason) {
        account.deduct(amount);
        pointAccountRepository.save(account);
        createPointTransaction(account, PointTransactionType.SPEND, amount, reason);
    }
}
