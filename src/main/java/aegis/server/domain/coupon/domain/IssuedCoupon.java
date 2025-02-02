package aegis.server.domain.coupon.domain;

import aegis.server.domain.member.domain.Member;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssuedCoupon {

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

    private Boolean isValid;

    private LocalDateTime usedAt;

    public static IssuedCoupon of(Coupon coupon, Member member) {
        return IssuedCoupon.builder()
                .coupon(coupon)
                .member(member)
                .isValid(true)
                .build();
    }

    public void use() {
        if (!isValid) {
            throw new CustomException(ErrorCode.COUPON_ALREADY_USED);
        }
        this.isValid = false;
        this.usedAt = LocalDateTime.now();
    }
}
