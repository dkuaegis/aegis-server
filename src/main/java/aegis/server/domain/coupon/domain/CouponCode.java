package aegis.server.domain.coupon.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_code_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_coupon_id")
    private IssuedCoupon issuedCoupon;

    private String code;

    private Boolean isValid;

    private LocalDateTime usedAt;

    public static CouponCode of(Coupon coupon, String code) {
        return CouponCode.builder().coupon(coupon).code(code).isValid(true).build();
    }

    public void use(IssuedCoupon issuedCoupon) {
        if (!isValid) {
            throw new CustomException(ErrorCode.COUPON_CODE_ALREADY_USED);
        }
        this.issuedCoupon = issuedCoupon;
        this.isValid = false;
        this.usedAt = LocalDateTime.now();
    }
}
