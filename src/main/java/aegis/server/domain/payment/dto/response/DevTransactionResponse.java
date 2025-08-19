package aegis.server.domain.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.payment.domain.Transaction;
import aegis.server.domain.payment.domain.TransactionType;

public record DevTransactionResponse(
        Long id,
        YearSemester yearSemester,
        LocalDateTime transactionTime,
        String depositorName,
        TransactionType transactionType,
        BigDecimal amount,
        BigDecimal balance,
        LocalDateTime createdAt) {

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
