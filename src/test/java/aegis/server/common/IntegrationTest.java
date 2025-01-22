package aegis.server.common;

import aegis.server.domain.coupon.domain.Coupon;
import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.coupon.repository.CouponRepository;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.domain.discord.service.listener.DiscordEventListener;
import aegis.server.domain.member.domain.*;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.payment.repository.TransactionRepository;
import aegis.server.domain.survey.domain.InterestField;
import aegis.server.domain.survey.dto.SurveyRequest;
import aegis.server.domain.survey.repository.SurveyRepository;
import aegis.server.global.security.dto.SessionUser;
import aegis.server.global.security.oidc.UserAuthInfo;
import net.dv8tion.jda.api.JDA;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTest {

    @Autowired
    protected DataBaseCleaner dataBaseCleaner;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected CouponRepository couponRepository;

    @Autowired
    protected IssuedCouponRepository issuedCouponRepository;

    @Autowired
    protected TransactionRepository transactionRepository;

    @Autowired
    protected SurveyRepository surveyRepository;

    @MockitoBean
    protected JDA jda;

    @MockitoBean
    protected DiscordEventListener discordEventListener;

    @BeforeEach
    void setUp() {
        dataBaseCleaner.clean();

        doNothing().when(discordEventListener).handlePaymentCompletedEvent(any());
        doNothing().when(discordEventListener).handleOverpaidEvent(any());
        doNothing().when(discordEventListener).handleMissingDepositorNameEvent(any());
    }

    protected Member createGuestMember() {
        Member member = Member.createGuestMember("123456789012345678901", "test@dankook.ac.kr", "테스트");
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

    protected SessionUser createSessionUser(Member member) {
        return SessionUser.from(UserAuthInfo.from(member));
    }

    protected Coupon createCoupon() {
        Coupon coupon = Coupon.create("테스트쿠폰", BigDecimal.valueOf(1000));
        couponRepository.save(coupon);
        ReflectionTestUtils.setField(coupon, "couponName", "테스트쿠폰" + coupon.getId());

        return couponRepository.save(coupon);
    }

    protected IssuedCoupon createIssuedCoupon(Member member, Coupon coupon) {
        return issuedCouponRepository.save(IssuedCoupon.of(coupon, member));
    }

    protected BigDecimal getCurrentCurrentDepositAmount(String depositorName) {
        return transactionRepository.sumAmountByDepositorName(depositorName);
    }

    protected SurveyRequest createSurveyRequest() {
        return SurveyRequest.builder()
                .interestFields(Set.of(InterestField.WEB_BACKEND, InterestField.WEB_FRONTEND))
                .interestEtc(Map.of(InterestField.WEB_ETC, "기타"))
                .registrationReason("등록 이유")
                .feedBack("피드백")
                .build();
    }
}
