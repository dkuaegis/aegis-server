package aegis.server.domain.activity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import aegis.server.domain.activity.domain.Activity;

public interface ActivityQueryRepository {

    Page<Activity> searchAdminActivities(String keyword, Pageable pageable, String orderByClause);
}
