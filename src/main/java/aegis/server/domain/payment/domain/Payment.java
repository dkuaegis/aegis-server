package aegis.server.domain.payment.domain;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.member.domain.Student;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static aegis.server.global.constant.Constant.CLUB_DUES;
import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

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

    private String expectedDepositorName;

    public static String expectedDepositorName(Student student) {
        return student.getMember().getName()
                + student.getStudentId().substring(student.getStudentId().length() - 6);
    }

    public static Payment of(Student student) {
        return Payment.builder()
                .student(student)
                .status(PaymentStatus.PENDING)
                .yearSemester(CURRENT_YEAR_SEMESTER)
                .originalPrice(CLUB_DUES)
                .finalPrice(CLUB_DUES)
                .expectedDepositorName(expectedDepositorName(student))
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

    public void confirmPayment(PaymentStatus status) {
        if (status.equals(PaymentStatus.PENDING)) {
            throw new CustomException(ErrorCode.PAYMENT_CANNOT_BE_CONFIRMED);
        }
        this.status = status;
        this.usedCoupons.forEach(issuedCoupon -> issuedCoupon.use(this));
    }
}
