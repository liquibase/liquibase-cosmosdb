package liquibase.ext.cosmosdb.changelog;

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

import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.core.H2Database;
import liquibase.executor.ExecutorService;
import liquibase.ext.cosmosdb.database.CosmosConnection;
import liquibase.ext.cosmosdb.database.CosmosLiquibaseDatabase;
import liquibase.nosql.executor.NoSqlExecutor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;


import static liquibase.plugin.Plugin.PRIORITY_SPECIALIZED;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CosmosHistoryServiceTest {

    protected CosmosLiquibaseDatabase cosmosLiquibaseDatabase;
    protected CosmosHistoryService cosmosHistoryService;
    protected NoSqlExecutor cosmosExecutor;
    protected CosmosConnection cosmosConnection;

    @BeforeEach
    protected void setUpEach() {
        cosmosConnection = new CosmosConnection();
        cosmosLiquibaseDatabase = new CosmosLiquibaseDatabase();
        cosmosLiquibaseDatabase.setConnection(cosmosConnection);
        cosmosHistoryService = (CosmosHistoryService) ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(cosmosLiquibaseDatabase);
        cosmosExecutor = (NoSqlExecutor) Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor(NoSqlExecutor.EXECUTOR_NAME, cosmosLiquibaseDatabase);
        cosmosHistoryService.reset();
        cosmosHistoryService.resetDeploymentId();
    }

    @Test
    void testGetPriority() {
        assertThat(cosmosHistoryService.getPriority()).isEqualTo(PRIORITY_SPECIALIZED);
    }

    @Test
    void testSupports() {
        assertThat(cosmosHistoryService.supports(cosmosLiquibaseDatabase)).isTrue();
        assertThat(cosmosHistoryService.supports(new H2Database())).isFalse();

    }

    @Test
    void testGetDatabaseChangeLogTableName() {
        assertThat(cosmosHistoryService.getDatabaseChangeLogTableName()).isEqualTo(cosmosLiquibaseDatabase.getDatabaseChangeLogTableName());
    }

    @Test
    void testCanCreateChangeLogTable() {
        assertThat(cosmosHistoryService.canCreateChangeLogTable()).isTrue();
    }

    @SneakyThrows
    @Test
    void testInit() {

    }

    @SneakyThrows
    @Test
    void testReset() {

    }

    @Test
    void testUpgradeChecksums() {
    }

    @Test
    void testGetRanChangeSets() {

    }

    @Test
    void testReplaceChecksum()  {

    }

    @Test
    void testGetRanChangeSet()  {

    }

    @Test
    void testSetExecType() {
    }

    @Test
    void testRemoveFromHistory()  {

    }

    @SneakyThrows
    @Test
    void testGetNextSequenceValue() {

    }

    @Test
    void testTag() {
    }

    @Test
    void testTagExists() {
    }

    @Test
    void testClearAllCheckSums() {
    }

    @Test
    void testDestroy() {

        
    }
}
