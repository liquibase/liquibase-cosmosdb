package liquibase.ext.cosmosdb.database;

/*-
 * #%L
 * Liquibase CosmosDB Extension
 * %%
 * Copyright (C) 2020 Mastercard
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.azure.cosmos.CosmosDatabase;
import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.DatabaseConnection;
import liquibase.exception.LiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.ext.cosmosdb.statement.DeleteAllContainersStatement;
import liquibase.nosql.database.AbstractNoSqlDatabase;
import liquibase.nosql.executor.NoSqlExecutor;

import java.util.Arrays;

import static java.util.Optional.ofNullable;

public class CosmosLiquibaseDatabase extends AbstractNoSqlDatabase {

    public static final String COSMOSDB_PRODUCT_NAME = "Cosmos DB";
    public static final String COSMOSDB_PRODUCT_SHORT_NAME = "cosmosdb";
    public static final int DEFAULT_PORT = 8081;

    public CosmosLiquibaseDatabase() {
    }

    @Override
    public void dropDatabaseObjects(final CatalogAndSchema schemaToDrop) throws LiquibaseException {
        final Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor(NoSqlExecutor.EXECUTOR_NAME, this);
        final DeleteAllContainersStatement deleteAllContainersStatement = new DeleteAllContainersStatement(Arrays.asList(getDatabaseChangeLogTableName(), getDatabaseChangeLogLockTableName()));
        executor.execute(deleteAllContainersStatement);
        ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).destroy();
    }

    @Override
    public String getDefaultDriver(final String url) {
        if (url.startsWith(CosmosConnectionString.COSMOSDB_PREFIX)) {
            return CosmosClientDriver.class.getName();
        }
        return null;
    }

    public CosmosDatabase getCosmosDatabase() {
        return ((CosmosConnection) getConnection()).getCosmosDatabase();
    }

    @Override
    public String getDatabaseProductName() {
        return COSMOSDB_PRODUCT_NAME;
    }

    /**
     * Returns an all-lower-case short name of the product.  Used for end-user selecting of database type
     * such as the DBMS precondition.
     */
    @Override
    public String getShortName() {
        return COSMOSDB_PRODUCT_SHORT_NAME;
    }

    @Override
    public Integer getDefaultPort() {
        return DEFAULT_PORT;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return COSMOSDB_PRODUCT_NAME;
    }

    @Override
    public String toString() {
        return getDatabaseProductName() + " : "
                + ofNullable(getConnection()).map(DatabaseConnection::getURL).orElse("NOT CONNECTED");
    }

}
