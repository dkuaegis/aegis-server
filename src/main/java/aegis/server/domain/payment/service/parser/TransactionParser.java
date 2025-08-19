package aegis.server.domain.payment.service.parser;

import aegis.server.domain.payment.domain.Transaction;

public interface TransactionParser {

    Transaction parse(String transactionLog);
}
