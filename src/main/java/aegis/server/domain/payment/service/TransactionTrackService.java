package aegis.server.domain.payment.service;

import aegis.server.domain.payment.service.parser.TransactionParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionTrackService {

    private final TransactionParser transactionParser;

    public void parseAndLogTransaction(String transactionLog) {
        log.info(transactionParser.parse(transactionLog).toString());
    }

}
