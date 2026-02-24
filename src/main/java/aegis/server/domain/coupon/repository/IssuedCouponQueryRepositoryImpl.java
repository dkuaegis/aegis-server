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

import aegis.server.domain.coupon.domain.IssuedCoupon;

@Repository
public class IssuedCouponQueryRepositoryImpl implements IssuedCouponQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<IssuedCoupon> searchAdminIssuedCoupons(
            String keyword, Long couponId, Long memberId, Boolean isValid, Pageable pageable, String orderByClause) {
        Map<String, Object> params = new LinkedHashMap<>();
        List<String> conditions = new ArrayList<>();

        if (keyword != null) {
            conditions.add("(LOWER(c.couponName) LIKE LOWER(CONCAT('%', :keyword, '%')) "
                    + "OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
                    + "OR LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%')) "
                    + "OR LOWER(COALESCE(m.studentId, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) "
                    + "OR STR(ic.id) LIKE CONCAT('%', :keyword, '%') "
                    + "OR STR(c.id) LIKE CONCAT('%', :keyword, '%') "
                    + "OR STR(m.id) LIKE CONCAT('%', :keyword, '%'))");
            params.put("keyword", keyword);
        }
        if (couponId != null) {
            conditions.add("c.id = :couponId");
            params.put("couponId", couponId);
        }
        if (memberId != null) {
            conditions.add("m.id = :memberId");
            params.put("memberId", memberId);
        }
        if (isValid != null) {
            conditions.add("ic.isValid = :isValid");
            params.put("isValid", isValid);
        }

        String whereClause = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);

        String selectJpql = "SELECT ic FROM IssuedCoupon ic " + "JOIN FETCH ic.coupon c " + "JOIN FETCH ic.member m "
                + "LEFT JOIN FETCH ic.payment p" + whereClause + " ORDER BY " + orderByClause;

        String countJpql = "SELECT COUNT(ic) FROM IssuedCoupon ic " + "JOIN ic.coupon c " + "JOIN ic.member m "
                + "LEFT JOIN ic.payment p" + whereClause;

        TypedQuery<IssuedCoupon> selectQuery = entityManager.createQuery(selectJpql, IssuedCoupon.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);

        for (Map.Entry<String, Object> param : params.entrySet()) {
            selectQuery.setParameter(param.getKey(), param.getValue());
            countQuery.setParameter(param.getKey(), param.getValue());
        }

        selectQuery.setFirstResult(Math.toIntExact(pageable.getOffset()));
        selectQuery.setMaxResults(pageable.getPageSize());

        List<IssuedCoupon> content = selectQuery.getResultList();
        Long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, total);
    }
}
