package aegis.server.domain.payment.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.member.domain.Member;

import static aegis.server.global.constant.Constant.CLUB_DUES;
import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "payment")
    @Builder.Default
    private List<IssuedCoupon> usedCoupons = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private YearSemester yearSemester;

    @Column(precision = 10, scale = 0)
    private BigDecimal originalPrice;

    @Column(precision = 10, scale = 0)
    private BigDecimal finalPrice;

    public static Payment of(Member member) {
        return Payment.builder()
                .member(member)
                .status(PaymentStatus.PENDING)
                .yearSemester(CURRENT_YEAR_SEMESTER)
                .originalPrice(CLUB_DUES)
                .finalPrice(CLUB_DUES)
                .build();
    }

    public static Payment createForDev(Member member, PaymentStatus status, YearSemester yearSemester) {
        return Payment.builder()
                .member(member)
                .status(status)
                .yearSemester(yearSemester)
                .originalPrice(CLUB_DUES)
                .finalPrice(CLUB_DUES)
                .build();
    }

    public void applyCoupons(List<IssuedCoupon> issuedCoupons) {
        this.usedCoupons.clear();
        this.usedCoupons.addAll(issuedCoupons);

        BigDecimal totalDiscountAmount = calculateTotalDiscountAmount(issuedCoupons);

        this.finalPrice = this.originalPrice.subtract(totalDiscountAmount);

        if (this.finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            this.finalPrice = BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateTotalDiscountAmount(List<IssuedCoupon> issuedCoupons) {
        return issuedCoupons.stream()
                .map(issuedCoupon -> issuedCoupon.getCoupon().getDiscountAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void completePayment() {
        this.status = PaymentStatus.COMPLETED;
        this.usedCoupons.forEach(issuedCoupon -> issuedCoupon.use(this));
    }

    public void updateForDev(PaymentStatus status, YearSemester yearSemester) {
        this.status = status;
        this.yearSemester = yearSemester;
    }
}
