package aegis.server.domain.point.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.point.domain.PointTransaction;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    @Query("SELECT pt FROM PointTransaction pt WHERE pt.pointAccount.id = :pointAccountId ORDER BY pt.id DESC")
    List<PointTransaction> findAllByPointAccountId(Long pointAccountId);
}
