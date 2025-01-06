package aegis.server.common;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DataBaseCleaner implements InitializingBean {

    @PersistenceContext
    private EntityManager em;

    private List<String> tableNames;

    @Override
    public void afterPropertiesSet() throws Exception {
        tableNames = em.getMetamodel().getEntities().stream()
                .map(EntityType::getName)
                .map(name -> name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase())
                .collect(Collectors.toList());
    }

    public void clean() {
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

        for (String tableName : tableNames) {
            em.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
            em.createNativeQuery("ALTER TABLE " + tableName + " ALTER COLUMN " + tableName + "_ID RESTART WITH 1").executeUpdate();
        }
        
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }
}
