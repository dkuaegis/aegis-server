package aegis.server.domain.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.payment.domain.Transaction;
import aegis.server.domain.payment.domain.TransactionType;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class DevTransactionResponse {

    private final Long id;
    private final YearSemester yearSemester;
    private final LocalDateTime transactionTime;
    private final String depositorName;
    private final TransactionType transactionType;
    private final BigDecimal amount;
    private final BigDecimal balance;
    private final LocalDateTime createdAt;

    public static DevTransactionResponse from(Transaction transaction) {
        return new DevTransactionResponse(
                transaction.getId(),
                transaction.getYearSemester(),
                transaction.getTransactionTime(),
                transaction.getDepositorName(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getBalance(),
                transaction.getCreatedAt());
    }
}
