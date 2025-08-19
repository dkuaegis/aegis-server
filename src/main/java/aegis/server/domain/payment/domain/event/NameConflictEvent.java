package aegis.server.domain.payment.domain.event;

import java.util.List;

import aegis.server.domain.payment.dto.internal.TransactionInfo;

public record NameConflictEvent(TransactionInfo transactionInfo, List<Long> memberIds) {}
