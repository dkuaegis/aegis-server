package aegis.server.domain.coupon.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.member.domain.Member;

public interface IssuedCouponRepository extends JpaRepository<IssuedCoupon, Long> {
    List<IssuedCoupon> findAllByMember(Member member);

    Optional<IssuedCoupon> findByIdAndMember(Long id, Member member);
}
