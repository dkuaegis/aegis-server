package aegis.server.domain.point.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.domain.PointTransactionType;

public interface PointTransactionQueryRepository {

    Page<PointTransaction> findAdminLedger(
            String memberKeyword,
            PointTransactionType transactionType,
            LocalDateTime fromDateTime,
            LocalDateTime toDateTime,
            Pageable pageable);
}
