package hu.blackbelt.osgi.liquibase;

/*-
 * #%L
 * OSGi liquibase
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import liquibase.exception.LiquibaseException;
import org.osgi.framework.Bundle;

import java.sql.Connection;
import java.util.Map;

public interface LiquibaseExecutor {

    /**
     * Execute the given script with liquibase from the given bundle with given connection.
     * @param connection
     * @param scriptPathInBundle
     * @param scriptBundle
     * @throws LiquibaseException
     */
    void executeLiquibaseScript(Connection connection, String scriptPathInBundle, Bundle scriptBundle) throws LiquibaseException;

    /**
     * Execute the given script with liquibase from the given bundle with given connection.
     * @param connection
     * @param scriptPathInBundle
     * @param scriptBundle
     * @param parameters
     * @throws LiquibaseException
     */
    void executeLiquibaseScript(Connection connection, String scriptPathInBundle, Bundle scriptBundle, Map<String, Object> parameters) throws LiquibaseException;

}
