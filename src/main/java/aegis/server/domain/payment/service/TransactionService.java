package aegis.server.domain.payment.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import aegis.server.domain.payment.domain.Transaction;
import aegis.server.domain.payment.domain.TransactionType;
import aegis.server.domain.payment.domain.event.TransactionCreatedEvent;
import aegis.server.domain.payment.dto.internal.TransactionInfo;
import aegis.server.domain.payment.repository.TransactionRepository;
import aegis.server.domain.payment.service.parser.TransactionParser;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionParser transactionParser;
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void createTransaction(String transactionLog) {
        Transaction transaction = transactionParser.parse(transactionLog);
        transactionRepository.save(transaction);

        logTransactionInfo(transaction);

        if (isDeposit(transaction)) {
            applicationEventPublisher.publishEvent(new TransactionCreatedEvent(TransactionInfo.from(transaction)));
        }
    }

    private void logTransactionInfo(Transaction transaction) {
        log.info(
                "[TransactionService] 거래 정보 저장 완료: transactionId={}, type={}, name={}, amount={}",
                transaction.getId(),
                transaction.getTransactionType(),
                transaction.getDepositorName(),
                transaction.getAmount());
    }

    private boolean isDeposit(Transaction transaction) {
        return transaction.getTransactionType().equals(TransactionType.DEPOSIT);
    }
}
