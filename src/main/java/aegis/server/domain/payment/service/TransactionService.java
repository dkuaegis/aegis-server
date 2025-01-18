package aegis.server.domain.payment.service;

import aegis.server.domain.member.domain.JoinProgress;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.domain.Transaction;
import aegis.server.domain.payment.domain.TransactionType;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.domain.payment.repository.TransactionRepository;
import aegis.server.domain.payment.service.parser.TransactionParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static aegis.server.global.constant.Constant.CURRENT_SEMESTER;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionParser transactionParser;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final JDA jda;

    @Value("${discord.guild-id}")
    private String guildId;

    @Value("${discord.complete-role-id}")
    private String roleId;

    @Transactional
    public void createTransaction(String transactionLog) {
        Transaction transaction = transactionParser.parse(transactionLog);
        transactionRepository.save(transaction);

        processTransaction(transaction);

        logTransactionInfo(transaction);
    }

    private void processTransaction(Transaction transaction) {
        if (transaction.getTransactionType().equals(TransactionType.WITHDRAWAL)) {
            return;
        }

        findAndUpdatePayment(transaction);
    }

    private void findAndUpdatePayment(Transaction transaction) {
        paymentRepository.findByExpectedDepositorNameAndCurrentSemesterAndStatus(transaction.getDepositorName(), CURRENT_SEMESTER, PaymentStatus.PENDING)
                .ifPresentOrElse(
                        payment -> updatePayment(payment, transaction),
                        () -> logMissingDepositorName(transaction)
                );
    }

    private void updatePayment(Payment payment, Transaction transaction) {
        BigDecimal currentDepositAmount = transactionRepository.sumAmountByDepositorName(payment.getExpectedDepositorName()).orElse(BigDecimal.ZERO);
        payment.updateStatus(currentDepositAmount);

        if (payment.getStatus().equals(PaymentStatus.COMPLETED)) {
            String discordId = payment.getMember().getDiscordId();
            Guild guild = jda.getGuildById(guildId);
            if (guild == null) {
                throw new IllegalStateException("서버를 찾을 수 없습니다");
            }
            Role role = guild.getRoleById(roleId);
            if (role == null) {
                throw new IllegalStateException("역할을 찾을 수 없습니다");
            }
            guild.addRoleToMember(UserSnowflake.fromId(discordId), role).queue();
            payment.getMember().updateJoinProgress(JoinProgress.COMPLETE);
        }
        if (payment.getStatus().equals(PaymentStatus.OVERPAID)) {
            logOverpaid(payment, transaction);
        }
    }

    private void logMissingDepositorName(Transaction transaction) {
        // TODO: DISCORD_ALARM: 입금자명과 일치하는 결제 정보가 없는 경우 디스코드 알림 필요
        log.warn("[TransactionService] 입금자명과 일치하는 결제 정보가 없습니다: name={}", transaction.getDepositorName());
    }

    private void logOverpaid(Payment payment, Transaction transaction) {
        // TODO: DISCORD_ALARM: 초과 입금이 발생한 경우 디스코드 알림 필요
        log.warn(
                "[TransactionService] 초과 입금이 발생하였습니다: paymentId={}, transactionId={}, name={}, amount={}, currentDepositAmount={}",
                payment.getId(), transaction.getId(), transaction.getDepositorName(), transaction.getAmount(), transactionRepository.sumAmountByDepositorName(transaction.getDepositorName())
        );
    }

    private void logTransactionInfo(Transaction transaction) {
        log.info(
                "[TransactionService] 거래 정보 저장 완료: transactionId={}, type={}, name={}, amount={}",
                transaction.getId(),
                transaction.getTransactionType(),
                transaction.getDepositorName(),
                transaction.getAmount()
        );
    }
}
