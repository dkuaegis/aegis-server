package aegis.server.domain.payment.domain.event;

import aegis.server.domain.payment.domain.Transaction;

public record MissingDepositorNameEvent(Transaction transaction) {
}
