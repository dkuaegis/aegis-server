package aegis.server.domain.coupon.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssuedCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issued_coupon_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Payment payment;

    private Boolean isValid;

    private LocalDateTime usedAt;

    public static IssuedCoupon of(Coupon coupon, Member member) {
        return IssuedCoupon.builder()
                .coupon(coupon)
                .member(member)
                .payment(null)
                .isValid(true)
                .build();
    }

    /**
     * 결제에 쿠폰을 임시로 연결합니다. 사용처리는 하지 않습니다.
     * 양방향 연관관계를 일관되게 유지합니다.
     */
    public void assignTo(Payment payment) {
        if (this.payment == payment) {
            return;
        }
        if (this.payment != null) {
            this.payment.getUsedCoupons().remove(this);
        }
        this.payment = payment;
        if (!payment.getUsedCoupons().contains(this)) {
            payment.getUsedCoupons().add(this);
        }
    }

    /**
     * 결제와의 임시 연결을 해제합니다. 사용처리 상태는 변경하지 않습니다.
     */
    public void detachFromPayment() {
        if (this.payment != null) {
            this.payment.getUsedCoupons().remove(this);
            this.payment = null;
        }
    }

    /**
     * 쿠폰을 실제로 사용 처리합니다.
     * 양방향 연관관계를 일관되게 유지합니다.
     * 이미 사용된 쿠폰은 사용할 수 없습니다.
     */
    public void use(Payment payment) {
        if (!isValid) {
            throw new CustomException(ErrorCode.COUPON_ALREADY_USED);
        }
        this.payment = payment;
        this.isValid = false;
        this.usedAt = LocalDateTime.now();
    }
}
