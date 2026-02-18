package aegis.server.domain.point.repository;

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

import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.domain.PointTransactionType;

@Repository
public class PointTransactionQueryRepositoryImpl implements PointTransactionQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<PointTransaction> findAdminLedger(
            String memberKeyword,
            PointTransactionType transactionType,
            LocalDateTime fromDateTime,
            LocalDateTime toDateTime,
            Pageable pageable) {
        Map<String, Object> params = new LinkedHashMap<>();
        List<String> conditions = new ArrayList<>();

        if (memberKeyword != null) {
            conditions.add("(LOWER(m.name) LIKE LOWER(CONCAT('%', :memberKeyword, '%')) "
                    + "OR LOWER(COALESCE(m.studentId, '')) LIKE LOWER(CONCAT('%', :memberKeyword, '%')))");
            params.put("memberKeyword", memberKeyword);
        }
        if (transactionType != null) {
            conditions.add("pt.transactionType = :transactionType");
            params.put("transactionType", transactionType);
        }
        if (fromDateTime != null) {
            conditions.add("pt.createdAt >= :fromDateTime");
            params.put("fromDateTime", fromDateTime);
        }
        if (toDateTime != null) {
            conditions.add("pt.createdAt < :toDateTime");
            params.put("toDateTime", toDateTime);
        }

        String whereClause = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);

        String selectJpql = "SELECT pt FROM PointTransaction pt "
                + "JOIN FETCH pt.pointAccount pa "
                + "JOIN FETCH pa.member m"
                + whereClause
                + " ORDER BY pt.id DESC";

        String countJpql = "SELECT COUNT(pt) FROM PointTransaction pt "
                + "JOIN pt.pointAccount pa "
                + "JOIN pa.member m"
                + whereClause;

        TypedQuery<PointTransaction> selectQuery = entityManager.createQuery(selectJpql, PointTransaction.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);

        for (Map.Entry<String, Object> param : params.entrySet()) {
            selectQuery.setParameter(param.getKey(), param.getValue());
            countQuery.setParameter(param.getKey(), param.getValue());
        }

        selectQuery.setFirstResult((int) pageable.getOffset());
        selectQuery.setMaxResults(pageable.getPageSize());

        List<PointTransaction> content = selectQuery.getResultList();
        Long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, total);
    }
}
