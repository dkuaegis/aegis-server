package aegis.server.domain.payment.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.payment.domain.Transaction;
import aegis.server.domain.payment.domain.TransactionType;

public interface TransactionQueryRepository {

    Page<Transaction> searchAdminTransactions(
            YearSemester yearSemester,
            TransactionType transactionType,
            String depositorKeyword,
            LocalDateTime fromDateTime,
            LocalDateTime toDateTime,
            Pageable pageable,
            String orderByClause);
}
