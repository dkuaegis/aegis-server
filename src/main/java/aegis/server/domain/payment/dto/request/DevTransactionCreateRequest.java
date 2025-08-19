package aegis.server.domain.payment.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import aegis.server.domain.payment.domain.TransactionType;

public record DevTransactionCreateRequest(
        @NotBlank String depositorName,
        @NotNull BigDecimal amount,
        @NotNull TransactionType transactionType,
        @NotNull BigDecimal balance) {}
