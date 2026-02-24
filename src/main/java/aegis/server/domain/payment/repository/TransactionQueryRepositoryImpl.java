package aegis.server.domain.payment.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.payment.domain.Transaction;
import aegis.server.domain.payment.domain.TransactionType;

@Repository
public class TransactionQueryRepositoryImpl implements TransactionQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Transaction> searchAdminTransactions(
            YearSemester yearSemester,
            TransactionType transactionType,
            String depositorKeyword,
            LocalDateTime fromDateTime,
            LocalDateTime toDateTime,
            Pageable pageable) {
        Map<String, Object> params = new LinkedHashMap<>();
        List<String> conditions = new ArrayList<>();

        if (yearSemester != null) {
            conditions.add("t.yearSemester = :yearSemester");
            params.put("yearSemester", yearSemester);
        }
        if (transactionType != null) {
            conditions.add("t.transactionType = :transactionType");
            params.put("transactionType", transactionType);
        }
        if (depositorKeyword != null) {
            conditions.add("LOWER(t.depositorName) LIKE LOWER(CONCAT('%', :depositorKeyword, '%'))");
            params.put("depositorKeyword", depositorKeyword);
        }
        if (fromDateTime != null) {
            conditions.add("t.transactionTime >= :fromDateTime");
            params.put("fromDateTime", fromDateTime);
        }
        if (toDateTime != null) {
            conditions.add("t.transactionTime < :toDateTime");
            params.put("toDateTime", toDateTime);
        }

        String whereClause = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);

        String selectJpql = "SELECT t FROM Transaction t" + whereClause + " ORDER BY t.transactionTime DESC, t.id DESC";
        String countJpql = "SELECT COUNT(t) FROM Transaction t" + whereClause;

        TypedQuery<Transaction> selectQuery = entityManager.createQuery(selectJpql, Transaction.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);

        for (Map.Entry<String, Object> param : params.entrySet()) {
            selectQuery.setParameter(param.getKey(), param.getValue());
            countQuery.setParameter(param.getKey(), param.getValue());
        }

        selectQuery.setFirstResult(Math.toIntExact(pageable.getOffset()));
        selectQuery.setMaxResults(pageable.getPageSize());

        List<Transaction> content = selectQuery.getResultList();
        Long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, total);
    }
}
