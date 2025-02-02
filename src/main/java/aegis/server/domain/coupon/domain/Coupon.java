package aegis.server.domain.coupon.domain;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"coupon_name", "discount_amount"})})
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    private String couponName;

    @Column(precision = 10, scale = 0)
    private BigDecimal discountAmount;

    public static Coupon create(String couponName, BigDecimal discountAmount) {
        if (discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.COUPON_DISCOUNT_AMOUNT_NOT_POSITIVE);
        }

        return Coupon.builder()
                .couponName(couponName)
                .discountAmount(discountAmount)
                .build();
    }
}
