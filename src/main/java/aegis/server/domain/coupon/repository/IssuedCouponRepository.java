package aegis.server.domain.coupon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.member.domain.Member;

public interface IssuedCouponRepository extends JpaRepository<IssuedCoupon, Long> {
    List<IssuedCoupon> findAllByMember(Member member);

    @Query(
            "SELECT COUNT(ic) FROM IssuedCoupon ic WHERE ic.id IN :ids AND ic.member.id = :memberId AND ic.isValid = true")
    long countByIdInAndMemberIdAndValid(List<Long> ids, Long memberId);

    @Query("SELECT ic FROM IssuedCoupon ic WHERE ic.id IN :ids AND ic.member.id = :memberId AND ic.isValid = true")
    List<IssuedCoupon> findByIdInAndMemberIdAndValid(List<Long> ids, Long memberId);
}
