package liquibase.ext.cosmosdb;

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


import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.DatabaseFactory;
import liquibase.executor.ExecutorService;
import liquibase.ext.cosmosdb.database.CosmosConnection;
import liquibase.ext.cosmosdb.database.CosmosLiquibaseDatabase;
import liquibase.lockservice.LockServiceFactory;
import liquibase.nosql.executor.NoSqlExecutor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static liquibase.ext.cosmosdb.TestUtils.DB_CONNECTION_URI_PROPERTY;
import static liquibase.nosql.executor.NoSqlExecutor.EXECUTOR_NAME;

/**
 * With {@link CosmosLiquibaseDatabase} initiated
 */
public abstract class AbstractCosmosWithConnectionIntegrationTest extends AbstractCosmosIntegrationTest {

    protected CosmosLiquibaseDatabase database;
    protected NoSqlExecutor executor;
    protected CosmosConnection cosmosConnection;
    protected CosmosDatabase cosmosDatabase;

    @BeforeAll
    protected void setUp() {
        super.setUp();

    }

    @BeforeEach
    @SneakyThrows
    protected void setUpEach() {
        resetServices();

        connectionString = testProperties.getProperty(DB_CONNECTION_URI_PROPERTY);
        database = (CosmosLiquibaseDatabase) DatabaseFactory.getInstance().openDatabase(connectionString, null, null, null , null);
        cosmosConnection = (CosmosConnection) database.getConnection();
        cosmosDatabase = cosmosConnection.getCosmosDatabase();
        executor = (NoSqlExecutor) Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor(EXECUTOR_NAME, database);

        deleteContainers();
    }

    @AfterEach
    @SneakyThrows
    protected void tearDownEach() {
        deleteContainers();
        resetServices();
        cosmosConnection.close();
    }

    protected void deleteContainers() {
        cosmosDatabase.readAllContainers().stream()
                .map(CosmosContainerProperties::getId).map(cosmosDatabase::getContainer).forEach(CosmosContainer::delete);
    }

    protected void resetServices() {
        LockServiceFactory.getInstance().resetAll();
        ChangeLogHistoryServiceFactory.getInstance().resetAll();
        Scope.getCurrentScope().getSingleton(ExecutorService.class).reset();
    }

}
