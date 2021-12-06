package hu.blackbelt.osgi.liquibase.impl;

import hu.blackbelt.osgi.liquibase.BundleResourceAccessor;
import hu.blackbelt.osgi.liquibase.LiquibaseExecutor;
import hu.blackbelt.osgi.liquibase.logging.Slf4jLogService;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Component(immediate = true)
@Slf4j
public class DefaultLiquibaseExecutor implements LiquibaseExecutor {

    private String scopeId;
    private BundleContext bundleContext;

    @Activate
    public void activate(BundleContext bundleContext) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        // Init
        Scope.getCurrentScope();

        Map<String, Object> scopeObjects = new HashMap<>();
        scopeObjects.put(Scope.Attr.logService.name(), new Slf4jLogService());
        scopeObjects.put(Scope.Attr.classLoader.name(), this.getClass().getClassLoader());
        scopeId = Scope.enter(scopeObjects);

        Thread.currentThread().setContextClassLoader(classLoader);
        this.bundleContext = bundleContext;

    }

    @Deactivate
    void deactivate() throws Exception {
        Scope.exit(scopeId);
    }

    @Override
    public synchronized void executeLiquibaseScript(Connection connection, String scriptPathInBundle, Bundle scriptBundle) throws LiquibaseException {
        executeLiquibaseScript(connection, scriptPathInBundle, scriptBundle, null);
    }

    @Override
    public void executeLiquibaseScript(Connection connection, String scriptPathInBundle, Bundle scriptBundle, Map<String, Object> parameters) throws LiquibaseException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

        try {
            BundleResourceAccessor bundleResourceAccessor = new BundleResourceAccessor(scriptBundle);
            JdbcConnection jdbcConnection = new JdbcConnection(connection);
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
            database.setConnection(jdbcConnection);

            final Liquibase liquibase = new Liquibase(scriptPathInBundle, bundleResourceAccessor, database);

            if (parameters != null) {
                parameters.entrySet().stream().forEach(e ->
                        liquibase.setChangeLogParameter(e.getKey(), e.getValue())
                );
            }
            log.info("Processing: {}", scriptPathInBundle);
            liquibase.update((String) null);
        } catch (LiquibaseException e) {
            throw e;
        } catch (Exception e) {
        } finally {
            try {
                Thread.currentThread().setContextClassLoader(classLoader);
            } catch (Exception e) {
            }
        }
    }
}
