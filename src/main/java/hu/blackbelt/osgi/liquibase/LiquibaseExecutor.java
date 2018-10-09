package hu.blackbelt.osgi.liquibase;

import liquibase.exception.LiquibaseException;
import org.osgi.framework.Bundle;

import java.sql.Connection;

public interface LiquibaseExecutor {

    /**
     * Execute the given script with liquibase from the given bundle with given connection.
     * @param connection
     * @param scriptPathInBundle
     * @param scriptBundle
     * @throws LiquibaseException
     */
    void executeLiquibaseScript(Connection connection, String scriptPathInBundle, Bundle scriptBundle) throws LiquibaseException;
}
