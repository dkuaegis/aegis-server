package aegis.server.domain.payment.service;

import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.domain.Transaction;
import aegis.server.domain.payment.domain.TransactionType;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.domain.payment.repository.TransactionRepository;
import aegis.server.domain.payment.service.parser.TransactionParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static aegis.server.global.constant.Constant.CURRENT_SEMESTER;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionParser transactionParser;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public void createTransaction(String transactionLog) {
        Transaction transaction = transactionParser.parse(transactionLog);
        transactionRepository.save(transaction);
        if (transaction.getTransactionType().equals(TransactionType.WITHDRAWAL)) {
            logTransactionInfo(transaction);
            return;
        }

        Optional<Payment> paymentOptional = paymentRepository.findByExpectedDepositorNameAndCurrentSemester(transaction.getName(), CURRENT_SEMESTER);

        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();
            payment.addTransaction(transaction);
            paymentRepository.save(payment);
            if (payment.getStatus().equals(PaymentStatus.OVERPAID)) {
                // TODO: DISCORD_ALARM: 초과 입금이 발생한 경우 디스코드 알림 필요
                log.warn(
                        "[TransactionService] 초과 입금이 발생하였습니다: paymentId={}, transactionId={}, name={}, amount={}, currentDepositAmount={}",
                        payment.getId(), transaction.getId(), transaction.getName(), transaction.getAmount(), payment.getCurrentDepositAmount()
                );
            }
        } else {
            // TODO: DISCORD_ALARM: 입금자명과 일치하는 결제 정보가 없는 경우 디스코드 알림 필요
            log.warn("[TransactionService] 입금자명과 일치하는 결제 정보가 없습니다: name={}", transaction.getName());
        }

        logTransactionInfo(transaction);
    }

    private void logTransactionInfo(Transaction transaction) {
        log.info(
                "[TransactionService] 거래 정보 저장 완료: transactionId={}, type={}, name={}, amount={}",
                transaction.getId(),
                transaction.getTransactionType(),
                transaction.getName(),
                transaction.getAmount()
        );
    }
}
