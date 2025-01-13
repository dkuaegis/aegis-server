package aegis.server.common;

import aegis.server.domain.coupon.domain.Coupon;
import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.coupon.repository.CouponRepository;
import aegis.server.domain.coupon.repository.IssuedCouponRepository;
import aegis.server.domain.member.domain.*;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.global.security.dto.SessionUser;
import aegis.server.global.security.oidc.UserAuthInfo;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Transactional
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

    @BeforeEach
    void cleanDatabase() {
        dataBaseCleaner.clean();
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
}
