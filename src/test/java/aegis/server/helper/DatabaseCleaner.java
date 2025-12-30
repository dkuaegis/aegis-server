package aegis.server.helper;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import org.hibernate.Session;

@Component
public class DatabaseCleaner implements InitializingBean {

    @PersistenceContext
    private EntityManager em;

    private List<String> tableNames;

    @Override
    public void afterPropertiesSet() {
        tableNames = em.getMetamodel().getEntities().stream()
                .map(EntityType::getName)
                .map(name -> name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase())
                .collect(Collectors.toList());
    }

    public void clean() {
        em.unwrap(Session.class).doWork(this::doClean);
    }

    private void doClean(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();

            if ("H2".equals(databaseProductName)) {
                // H2 specific syntax
                statement.executeUpdate("SET REFERENTIAL_INTEGRITY FALSE");
                for (String tableName : tableNames) {
                    statement.executeUpdate("TRUNCATE TABLE " + tableName + " RESTART IDENTITY");
                }
                statement.executeUpdate("SET REFERENTIAL_INTEGRITY TRUE");
            } else if ("PostgreSQL".equals(databaseProductName)) {
                // PostgreSQL specific syntax
                statement.executeUpdate("SET session_replication_role = replica");
                for (String tableName : tableNames) {
                    statement.executeUpdate("TRUNCATE TABLE " + tableName + " RESTART IDENTITY CASCADE");
                }
                statement.executeUpdate("SET session_replication_role = DEFAULT");
            } else {
                // Fallback for other databases
                for (String tableName : tableNames) {
                    statement.executeUpdate("DELETE FROM " + tableName);
                }
            }
        }
    }
}
