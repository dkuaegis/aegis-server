package aegis.server.domain.payment.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    @Column(name = "transaction_id")
    private Long id;

    // === 은행앱에서 발송한 거래 내역 정보 START ===

    private LocalDateTime transactionTime;

    private String depositorName;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private Long amount;

    private Long balance;

    // === 은행앱에서 발송한 거래 내역 정보 END ===

    public static Transaction of(
            LocalDateTime transactionTime,
            String depositorName,
            TransactionType transactionType,
            Long amount,
            Long balance
    ) {
        return Transaction.builder()
                .transactionTime(transactionTime)
                .depositorName(depositorName)
                .transactionType(transactionType)
                .amount(amount)
                .balance(balance)
                .build();
    }
}
