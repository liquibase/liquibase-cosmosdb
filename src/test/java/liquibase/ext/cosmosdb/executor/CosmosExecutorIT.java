package liquibase.ext.cosmosdb.executor;

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
import liquibase.executor.ExecutorService;
import liquibase.ext.cosmosdb.AbstractCosmosWithConnectionIntegrationTest;
import liquibase.nosql.executor.NoSqlExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CosmosExecutorIT extends AbstractCosmosWithConnectionIntegrationTest {

    protected NoSqlExecutor cosmosExecutor;

    @BeforeEach
    protected void setUpEach() {
        super.setUpEach();
        cosmosExecutor = (NoSqlExecutor) Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor(NoSqlExecutor.EXECUTOR_NAME, database);
    }

    @Test
    void testGetInstance() {
        //final Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor(database);
        //assertThat(executor, notNullValue());
        //assertThat(executor, instanceOf(CosmosExecutor.class));
    }

    @Test
    void queryForObject() {
    }

    @Test
    void queryForLong() {
    }

    @Test
    void queryForInt() {
    }

    @Test
    void queryForList() {
    }

    @Test
    void execute() {
    }

    @Test
    void update() {
    }

    @Test
    void updatesDatabase() {
    }

}
