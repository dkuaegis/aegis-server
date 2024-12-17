package aegis.server.domain.bank.service.parser;

import aegis.server.domain.bank.domain.Transaction;

public interface TransactionParser {

    Transaction parse(String transactionLog);
    
}
