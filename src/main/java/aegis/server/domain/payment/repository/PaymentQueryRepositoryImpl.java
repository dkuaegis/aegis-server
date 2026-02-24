package aegis.server.domain.payment.repository;

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
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;

@Repository
public class PaymentQueryRepositoryImpl implements PaymentQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Payment> searchAdminPayments(
            YearSemester yearSemester, PaymentStatus status, String memberKeyword, Pageable pageable) {
        Map<String, Object> params = new LinkedHashMap<>();
        List<String> conditions = new ArrayList<>();

        if (yearSemester != null) {
            conditions.add("p.yearSemester = :yearSemester");
            params.put("yearSemester", yearSemester);
        }
        if (status != null) {
            conditions.add("p.status = :status");
            params.put("status", status);
        }
        if (memberKeyword != null) {
            conditions.add("(LOWER(m.name) LIKE LOWER(CONCAT('%', :memberKeyword, '%')) "
                    + "OR LOWER(COALESCE(m.studentId, '')) LIKE LOWER(CONCAT('%', :memberKeyword, '%')))");
            params.put("memberKeyword", memberKeyword);
        }

        String whereClause = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);

        String selectJpql = "SELECT p FROM Payment p " + "JOIN FETCH p.member m" + whereClause + " ORDER BY p.id DESC";

        String countJpql = "SELECT COUNT(p) FROM Payment p " + "JOIN p.member m" + whereClause;

        TypedQuery<Payment> selectQuery = entityManager.createQuery(selectJpql, Payment.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);

        for (Map.Entry<String, Object> param : params.entrySet()) {
            selectQuery.setParameter(param.getKey(), param.getValue());
            countQuery.setParameter(param.getKey(), param.getValue());
        }

        selectQuery.setFirstResult(Math.toIntExact(pageable.getOffset()));
        selectQuery.setMaxResults(pageable.getPageSize());

        List<Payment> content = selectQuery.getResultList();
        Long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, total);
    }
}
