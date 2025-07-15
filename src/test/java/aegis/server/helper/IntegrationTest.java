package aegis.server.helper;

import java.math.BigDecimal;

import net.dv8tion.jda.api.JDA;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.BeforeEach;

import aegis.server.domain.coupon.domain.Coupon;
import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.coupon.repository.CouponRepository;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.domain.discord.service.listener.DiscordEventListener;
import aegis.server.domain.member.domain.*;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.global.security.oidc.UserDetails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@ActiveProfiles("test")
public class IntegrationTest {

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

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
        redisCleaner.clean();

        doNothing().when(discordEventListener).handlePaymentCompletedEvent(any());
        doNothing().when(discordEventListener).handleMismatchEvent(any());
    }

    protected Member createInitialMember() {
        Member member = Member.create("12345678901234567890", "test@dankook.ac.kr", "테스트사용자이름");
        memberRepository.save(member);

        // 한 개의 테스트 케이스에서 여러 번 호출되도 Member 엔티티의 고유성을 위하여 Reflection을 사용하여 수정
        ReflectionTestUtils.setField(member, "oidcId", member.getOidcId() + member.getId());
        ReflectionTestUtils.setField(member, "email", "test" + member.getId() + "@dankook.ac.kr");
        ReflectionTestUtils.setField(member, "name", "테스트사용자이름" + member.getId());

        return memberRepository.save(member);
    }

    protected Member createMember() {
        Member member = createInitialMember();
        member.updatePersonalInfo(
                "010-1234-5678", "32000001", Department.SW융합대학_컴퓨터공학과, Grade.THREE, "010101", Gender.MALE);

        return memberRepository.save(member);
    }

    protected UserDetails createUserDetails(Member member) {
        return UserDetails.from(member);
    }

    protected Coupon create5000DiscountCoupon() {
        Coupon coupon = Coupon.create("테스트쿠폰", BigDecimal.valueOf(5000L));
        couponRepository.save(coupon);
        ReflectionTestUtils.setField(coupon, "couponName", "테스트쿠폰" + coupon.getId());

        return couponRepository.save(coupon);
    }

    protected IssuedCoupon createIssuedCoupon(Member member, Coupon coupon) {
        return issuedCouponRepository.save(IssuedCoupon.of(coupon, member));
    }
}
