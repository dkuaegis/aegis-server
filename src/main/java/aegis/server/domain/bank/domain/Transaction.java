package aegis.server.domain.bank.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
public class Transaction {

    private LocalDateTime transactionTime;
    private String depositorName;
    private TransactionType transactionType;
    private Long amount;
    private Long balance;

}
