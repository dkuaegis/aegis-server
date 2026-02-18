package aegis.server.domain.member.repository;

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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.MemberRecord;
import aegis.server.domain.member.domain.Role;

@Repository
public class MemberRecordQueryRepositoryImpl implements MemberRecordQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<MemberRecord> searchByYearSemesterForAdmin(
            YearSemester yearSemester, String keyword, Role role, Pageable pageable) {
        Map<String, Object> params = new LinkedHashMap<>();
        List<String> conditions = new ArrayList<>();

        conditions.add("mr.yearSemester = :yearSemester");
        params.put("yearSemester", yearSemester);

        if (keyword != null) {
            conditions.add("(LOWER(mr.snapshotName) LIKE LOWER(CONCAT('%', :keyword, '%')) "
                    + "OR LOWER(COALESCE(mr.snapshotStudentId, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) "
                    + "OR LOWER(mr.snapshotEmail) LIKE LOWER(CONCAT('%', :keyword, '%')))");
            params.put("keyword", keyword);
        }
        if (role != null) {
            conditions.add("mr.snapshotRole = :role");
            params.put("role", role);
        }

        String whereClause = " WHERE " + String.join(" AND ", conditions);

        String selectJpql = "SELECT mr FROM MemberRecord mr "
                + "JOIN FETCH mr.member m"
                + whereClause
                + buildOrderByClause(pageable.getSort());
        String countJpql = "SELECT COUNT(mr) FROM MemberRecord mr" + whereClause;

        TypedQuery<MemberRecord> selectQuery = entityManager.createQuery(selectJpql, MemberRecord.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);

        for (Map.Entry<String, Object> param : params.entrySet()) {
            selectQuery.setParameter(param.getKey(), param.getValue());
            countQuery.setParameter(param.getKey(), param.getValue());
        }

        selectQuery.setFirstResult((int) pageable.getOffset());
        selectQuery.setMaxResults(pageable.getPageSize());

        List<MemberRecord> content = selectQuery.getResultList();
        Long total = countQuery.getSingleResult();
        return new PageImpl<>(content, pageable, total);
    }

    private String buildOrderByClause(Sort sort) {
        if (sort.isUnsorted()) {
            return " ORDER BY mr.id ASC";
        }

        List<String> orderFragments = new ArrayList<>();
        for (Sort.Order order : sort) {
            String property = mapSortProperty(order.getProperty());
            String direction = order.isAscending() ? "ASC" : "DESC";
            orderFragments.add(property + " " + direction);
        }

        return " ORDER BY " + String.join(", ", orderFragments);
    }

    private String mapSortProperty(String property) {
        return switch (property) {
            case "id" -> "mr.id";
            case "snapshotName" -> "mr.snapshotName";
            default -> throw new IllegalArgumentException("Unsupported sort property: " + property);
        };
    }
}
