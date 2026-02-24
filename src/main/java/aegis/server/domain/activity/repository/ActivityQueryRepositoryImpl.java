package aegis.server.domain.activity.repository;

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

import aegis.server.domain.activity.domain.Activity;

@Repository
public class ActivityQueryRepositoryImpl implements ActivityQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Activity> searchAdminActivities(String keyword, Pageable pageable, String orderByClause) {
        Map<String, Object> params = new LinkedHashMap<>();
        List<String> conditions = new ArrayList<>();

        if (keyword != null) {
            conditions.add("(LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
                    + "OR STR(a.id) LIKE CONCAT('%', :keyword, '%') "
                    + "OR STR(a.pointAmount) LIKE CONCAT('%', :keyword, '%'))");
            params.put("keyword", keyword);
        }

        String whereClause = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);

        String selectJpql = "SELECT a FROM Activity a" + whereClause + " ORDER BY " + orderByClause;
        String countJpql = "SELECT COUNT(a) FROM Activity a" + whereClause;

        TypedQuery<Activity> selectQuery = entityManager.createQuery(selectJpql, Activity.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);

        for (Map.Entry<String, Object> param : params.entrySet()) {
            selectQuery.setParameter(param.getKey(), param.getValue());
            countQuery.setParameter(param.getKey(), param.getValue());
        }

        selectQuery.setFirstResult(Math.toIntExact(pageable.getOffset()));
        selectQuery.setMaxResults(pageable.getPageSize());

        List<Activity> content = selectQuery.getResultList();
        Long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, total);
    }
}
