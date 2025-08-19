package aegis.server.domain.payment.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import aegis.server.domain.payment.domain.Transaction;
import aegis.server.domain.payment.domain.TransactionType;
import aegis.server.domain.payment.domain.event.TransactionCreatedEvent;
import aegis.server.domain.payment.dto.internal.TransactionInfo;
import aegis.server.domain.payment.dto.request.DevTransactionCreateRequest;
import aegis.server.domain.payment.dto.response.DevTransactionResponse;
import aegis.server.domain.payment.repository.TransactionRepository;

@Slf4j
@Service
@Profile({"dev", "local", "test"})
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DevTransactionService {

    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public DevTransactionResponse createTransaction(DevTransactionCreateRequest request) {
        Transaction transaction = Transaction.createForDev(
                request.depositorName(), request.transactionType(), request.amount(), request.balance());

        transactionRepository.save(transaction);
        logTransactionInfo(transaction);

        if (isDeposit(transaction)) {
            applicationEventPublisher.publishEvent(new TransactionCreatedEvent(TransactionInfo.from(transaction)));
        }

        return DevTransactionResponse.from(transaction);
    }

    private void logTransactionInfo(Transaction transaction) {
        log.info(
                "[DevTransactionService] 개발용 거래 정보 저장 완료: transactionId={}, type={}, name={}, amount={}",
                transaction.getId(),
                transaction.getTransactionType(),
                transaction.getDepositorName(),
                transaction.getAmount());
    }

    private boolean isDeposit(Transaction transaction) {
        return transaction.getTransactionType() == TransactionType.DEPOSIT;
    }
}
