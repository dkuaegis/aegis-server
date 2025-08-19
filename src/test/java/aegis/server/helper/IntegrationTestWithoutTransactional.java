package aegis.server.helper;

import net.dv8tion.jda.api.JDA;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.junit.jupiter.api.AfterEach;

import aegis.server.domain.coupon.repository.CouponRepository;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.domain.discord.service.listener.DiscordEventListener;
import aegis.server.domain.member.domain.*;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.member.service.listener.SessionInvalidator;
import aegis.server.global.security.oidc.UserDetails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@ActiveProfiles("test")
public class IntegrationTestWithoutTransactional {

    @Autowired
    DatabaseCleaner databaseCleaner;

    @Autowired
    RedisCleaner redisCleaner;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    IssuedCouponRepository issuedCouponRepository;

    @MockitoBean
    JDA jda;

    @MockitoBean
    DiscordEventListener discordEventListener;

    @MockitoBean
    SessionInvalidator sessionInvalidator;

    @AfterEach
    void setUp() {
        databaseCleaner.clean();
        redisCleaner.clean();

        doNothing().when(discordEventListener).handlePaymentCompletedEvent(any());
        doNothing().when(discordEventListener).handleMismatchEvent(any());
    }

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
}
