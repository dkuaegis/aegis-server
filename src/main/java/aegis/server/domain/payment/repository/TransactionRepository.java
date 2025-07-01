package aegis.server.domain.payment.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.payment.domain.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // 만약 입금자명으로 조회된 거래 내역이 없다면 0을 반환한다. (COALESCE를 통한 NULL 처리)
    // 따라서 Optional을 사용하지 않는다.
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.depositorName = :depositorName")
    BigDecimal sumAmountByDepositorName(String depositorName);
}
