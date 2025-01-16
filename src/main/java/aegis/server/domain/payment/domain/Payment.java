package aegis.server.domain.payment.domain;

import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static aegis.server.global.constant.Constant.CLUB_DUES;
import static aegis.server.global.constant.Constant.CURRENT_SEMESTER;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "payment")
    @Builder.Default
    private List<IssuedCoupon> usedCoupons = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Comment("정상 회비")
    @Column(precision = 10, scale = 0)
    private BigDecimal originalPrice;

    @Comment("총 할인 금액")
    @Column(precision = 10, scale = 0)
    private BigDecimal totalDiscountAmount;

    @Comment("최종 회비")
    @Column(precision = 10, scale = 0)
    private BigDecimal finalPrice;

    @Comment("현재 학기")
    private String currentSemester;

    @Comment("요구되는 입금자명")
    private String expectedDepositorName;

    public static String expectedDepositorName(Member member) {
        return member.getName()
                + member.getStudentId().substring(member.getStudentId().length() - 6);
    }

    public static Payment of(
            Member member
    ) {
        return Payment.builder()
                .member(member)
                .status(PaymentStatus.PENDING)
                .originalPrice(CLUB_DUES)
                .totalDiscountAmount(BigDecimal.ZERO)
                .finalPrice(CLUB_DUES)
                .currentSemester(CURRENT_SEMESTER)
                .expectedDepositorName(expectedDepositorName(member))
                .build();
    }

    public void useCoupons(List<IssuedCoupon> coupons) {
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (IssuedCoupon coupon : coupons) {
            coupon.use(this);
            totalDiscount = totalDiscount.add(coupon.getCoupon().getDiscountAmount());
        }

        totalDiscountAmount = totalDiscount;
        finalPrice = originalPrice.subtract(totalDiscount);

        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }
    }

//    private void updateStatus() {
//        if (finalPrice.compareTo(currentDepositAmount) == 0) {
//            status = PaymentStatus.COMPLETED;
//        } else if (finalPrice.compareTo(currentDepositAmount) < 0) {
//            status = PaymentStatus.OVERPAID;
//        }
//    }

    public void cancel() {
        status = PaymentStatus.CANCELED;
    }
}
