package aegis.server.domain.payment.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import aegis.server.domain.payment.domain.TransactionType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DevTransactionCreateRequest {

    private String depositorName;
    private BigDecimal amount;
    private TransactionType transactionType;
    private BigDecimal balance;
}
