package aegis.server.domain.bank.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@ToString
public class Transaction {

    private LocalDateTime transactionTime;
    private String name;
    private TransactionType transactionType;
    private Long amount;
    private Long balance;

    public static Transaction of(LocalDateTime transactionTime, String name, TransactionType transactionType, Long amount, Long balance) {
        return Transaction.builder()
                .transactionTime(transactionTime)
                .name(name)
                .transactionType(transactionType)
                .amount(amount)
                .balance(balance)
                .build();
    }
}
