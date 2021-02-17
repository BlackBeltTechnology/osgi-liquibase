package hu.blackbelt.osgi.liquibase.impl;

import hu.blackbelt.osgi.liquibase.BundleResourceAccessor;
import hu.blackbelt.osgi.liquibase.LiquibaseExecutor;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;

import java.sql.Connection;
import java.util.Map;

@Component(immediate = true)
@Slf4j
public class DefaultLiquibaseExecutor implements LiquibaseExecutor {

    @Override
    public synchronized void executeLiquibaseScript(Connection connection, String scriptPathInBundle, Bundle scriptBundle) throws LiquibaseException {
        executeLiquibaseScript(connection, scriptPathInBundle, scriptBundle, null);
    }

    @Override
    public void executeLiquibaseScript(Connection connection, String scriptPathInBundle, Bundle scriptBundle, Map<String, Object> parameters) throws LiquibaseException {
        JdbcConnection jdbcConnection = new JdbcConnection(connection);
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
        database.setConnection(jdbcConnection);

        final Liquibase liquibase = new Liquibase(scriptPathInBundle,
                new BundleResourceAccessor(scriptBundle), database);

        if (parameters != null) {
            parameters.entrySet().stream().forEach(e ->
                liquibase.setChangeLogParameter(e.getKey(), e.getValue())
            );
        }
        log.info("Processing: {}", scriptPathInBundle);
        liquibase.update((String) null);
    }
}
