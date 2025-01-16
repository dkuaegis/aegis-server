package aegis.server.domain.payment.repository;

import aegis.server.domain.payment.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.depositorName = :depositorName")
    Optional<Long> sumAmountByDepositorName(String depositorName);
}
