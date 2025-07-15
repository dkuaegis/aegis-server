package aegis.server.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.payment.domain.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {}
