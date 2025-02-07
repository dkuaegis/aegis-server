package aegis.server.domain.payment.domain;

import aegis.server.domain.common.domain.YearSemester;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
                @Index(name = "idx_transaction_depositor_name", columnList = "depositorName"),
        }
)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private YearSemester yearSemester;

    // === 은행앱에서 발송한 거래 내역 정보 ===

    private LocalDateTime transactionTime;

    private String depositorName;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(precision = 10, scale = 0)
    private BigDecimal amount;

    @Column(precision = 10, scale = 0)
    private BigDecimal balance;

    public static Transaction of(
            LocalDateTime transactionTime,
            String depositorName,
            TransactionType transactionType,
            BigDecimal amount,
            BigDecimal balance
    ) {
        return Transaction.builder()
                .yearSemester(CURRENT_YEAR_SEMESTER)
                .transactionTime(transactionTime)
                .depositorName(depositorName)
                .transactionType(transactionType)
                .amount(amount)
                .balance(balance)
                .build();
    }
}
