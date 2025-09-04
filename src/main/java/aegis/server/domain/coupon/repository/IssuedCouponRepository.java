package aegis.server.domain.coupon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.member.domain.Member;

public interface IssuedCouponRepository extends JpaRepository<IssuedCoupon, Long> {

    @Query("SELECT ic FROM IssuedCoupon ic JOIN FETCH ic.member JOIN FETCH ic.coupon WHERE ic.member = :member")
    List<IssuedCoupon> findAllByMemberWithCoupon(Member member);

    @Query("SELECT ic FROM IssuedCoupon ic JOIN FETCH ic.member JOIN FETCH ic.coupon "
            + "WHERE ic.member = :member AND ic.isValid = true")
    List<IssuedCoupon> findAllByMemberAndIsValidTrueWithCoupon(Member member);

    @Query(
            "SELECT COUNT(ic) FROM IssuedCoupon ic WHERE ic.id IN :ids AND ic.member.id = :memberId AND ic.isValid = true")
    long countValidByIdInAndMemberId(List<Long> ids, Long memberId);

    @Query("SELECT ic FROM IssuedCoupon ic WHERE ic.id IN :ids AND ic.member.id = :memberId AND ic.isValid = true")
    List<IssuedCoupon> findByIdInAndMemberIdAndValid(List<Long> ids, Long memberId);

    @Query("SELECT ic FROM IssuedCoupon ic WHERE ic.payment.id = :paymentId")
    List<IssuedCoupon> findAllByPaymentId(Long paymentId);
}
