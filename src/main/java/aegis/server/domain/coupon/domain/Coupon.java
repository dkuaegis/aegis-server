package aegis.server.domain.coupon.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    private String couponName;

    @Column(precision = 10, scale = 0)
    private BigDecimal discountAmount;

    public static Coupon create(String couponName, BigDecimal discountAmount) {
        if (discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("할인 금액은 0보다 커야 합니다.");
        }

        return Coupon.builder()
                .couponName(couponName)
                .discountAmount(discountAmount)
                .build();
    }
}
