package aegis.server.domain.coupon.repository;

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

import aegis.server.domain.coupon.domain.Coupon;

@Repository
public class CouponQueryRepositoryImpl implements CouponQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Coupon> searchAdminCoupons(String keyword, Pageable pageable, String orderByClause) {
        Map<String, Object> params = new LinkedHashMap<>();
        List<String> conditions = new ArrayList<>();

        if (keyword != null) {
            conditions.add("(LOWER(c.couponName) LIKE LOWER(CONCAT('%', :keyword, '%')) "
                    + "OR STR(c.id) LIKE CONCAT('%', :keyword, '%') "
                    + "OR STR(c.discountAmount) LIKE CONCAT('%', :keyword, '%'))");
            params.put("keyword", keyword);
        }

        String whereClause = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);

        String selectJpql = "SELECT c FROM Coupon c" + whereClause + " ORDER BY " + orderByClause;
        String countJpql = "SELECT COUNT(c) FROM Coupon c" + whereClause;

        TypedQuery<Coupon> selectQuery = entityManager.createQuery(selectJpql, Coupon.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);

        for (Map.Entry<String, Object> param : params.entrySet()) {
            selectQuery.setParameter(param.getKey(), param.getValue());
            countQuery.setParameter(param.getKey(), param.getValue());
        }

        selectQuery.setFirstResult(Math.toIntExact(pageable.getOffset()));
        selectQuery.setMaxResults(pageable.getPageSize());

        List<Coupon> content = selectQuery.getResultList();
        Long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, total);
    }
}
