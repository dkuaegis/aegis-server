package aegis.server.domain.coupon.repository;

import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssuedCouponRepository extends JpaRepository<IssuedCoupon, Long> {
    List<IssuedCoupon> findByMember(Member member);
}
