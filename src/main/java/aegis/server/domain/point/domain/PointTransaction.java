package aegis.server.domain.point.domain;

import java.math.BigDecimal;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.common.domain.YearSemester;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_transaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private PointAccount pointAccount;

    @Enumerated(EnumType.STRING)
    private YearSemester yearSemester;

    @Enumerated(EnumType.STRING)
    private PointTransactionType transactionType;

    @Column(precision = 10, scale = 0)
    private BigDecimal amount;

    private String reason;

    public static PointTransaction create(
            PointAccount pointAccount, PointTransactionType transactionType, BigDecimal amount, String reason) {
        return PointTransaction.builder()
                .pointAccount(pointAccount)
                .yearSemester(CURRENT_YEAR_SEMESTER)
                .transactionType(transactionType)
                .amount(amount)
                .reason(reason)
                .build();
    }
}
