package aegis.server.domain.payment.repository;

import aegis.server.domain.payment.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
