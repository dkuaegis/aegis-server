package aegis.server.domain.coupon.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Entity
@Table(
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_coupon_code_code", columnNames = "code"),
            @UniqueConstraint(name = "uk_coupon_code_issued_coupon", columnNames = "issued_coupon_id")
        })
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
    @JoinColumn(name = "coupon_id", foreignKey = @ForeignKey(name = "fk_coupon_code_coupon"))
    private Coupon coupon;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_coupon_id", foreignKey = @ForeignKey(name = "fk_coupon_code_issued_coupon"))
    private IssuedCoupon issuedCoupon;

    private String code;

    @Column(length = 255)
    private String description;

    private Boolean isValid;

    private LocalDateTime usedAt;

    public static CouponCode of(Coupon coupon, String code, String description) {
        return CouponCode.builder()
                .coupon(coupon)
                .code(code)
                .description(description)
                .isValid(true)
                .build();
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
