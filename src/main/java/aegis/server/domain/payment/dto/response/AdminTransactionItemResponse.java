package aegis.server.domain.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.payment.domain.Transaction;
import aegis.server.domain.payment.domain.TransactionType;

public record AdminTransactionItemResponse(
        Long transactionId,
        YearSemester yearSemester,
        LocalDateTime transactionTime,
        String depositorName,
        TransactionType transactionType,
        BigDecimal amount,
        BigDecimal balance,
        LocalDateTime createdAt) {

    public static AdminTransactionItemResponse from(Transaction transaction) {
        return new AdminTransactionItemResponse(
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
