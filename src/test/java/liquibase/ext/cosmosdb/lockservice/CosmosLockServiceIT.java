package liquibase.ext.cosmosdb.lockservice;

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
import liquibase.database.core.PostgresDatabase;
import liquibase.executor.ExecutorService;
import liquibase.ext.cosmosdb.AbstractCosmosWithConnectionIntegrationTest;
import liquibase.ext.cosmosdb.database.CosmosLiquibaseDatabase;
import liquibase.ext.cosmosdb.statement.CountContainersByNameStatement;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.lockservice.LockServiceFactory;
import liquibase.nosql.executor.NoSqlExecutor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE;
import static org.assertj.core.api.Assertions.assertThat;

class CosmosLockServiceIT extends AbstractCosmosWithConnectionIntegrationTest {

    public CosmosLockService cosmosLockService;
    private CountContainersByNameStatement countContainersByNameStatement;
    protected NoSqlExecutor cosmosExecutor;

    @BeforeEach
    protected void setUpEach() {
        super.setUpEach();
        cosmosLockService = (CosmosLockService) LockServiceFactory.getInstance().getLockService(database);
        cosmosExecutor = (NoSqlExecutor) Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor(NoSqlExecutor.EXECUTOR_NAME, database);
        cosmosLockService.reset();
        countContainersByNameStatement = new CountContainersByNameStatement(database.getDatabaseChangeLogLockTableName());
    }

    @SneakyThrows
    @Test
    void testInit() {
        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.hasChangeLogLock()).isFalse();
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isNull();
        cosmosLockService.init();
        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.hasChangeLogLock()).isFalse();
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isTrue();
        assertThat(countContainersByNameStatement.queryForLong(database)).isEqualTo(1L);
    }

    @SneakyThrows
    @Test
    void testReset() {
        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.hasChangeLogLock()).isFalse();
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isNull();
        cosmosLockService.init();
        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.hasChangeLogLock()).isFalse();
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isTrue();
        cosmosLockService.reset();
        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.hasChangeLogLock()).isFalse();
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isNull();
    }

    @SneakyThrows
    @Test
    void testAcquireLock() {

        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.hasChangeLogLock()).isFalse();
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isNull();
        final boolean acquiredLock = cosmosLockService.acquireLock();
        assertThat(acquiredLock).isTrue();
        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isTrue();
        assertThat(cosmosLockService.hasChangeLogLock()).isTrue();
        final DatabaseChangeLogLock[] locks = cosmosLockService.listLocks();
        assertThat(locks).hasSize(1);
        assertThat(((CosmosChangeLogLock)locks[0]).getLocked()).isTrue();
        // Reacquire lock
        cosmosLockService.reset();
        final boolean reacquiredLock = cosmosLockService.acquireLock();
        assertThat(reacquiredLock).isFalse();
        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isTrue();
        assertThat(cosmosLockService.hasChangeLogLock()).isFalse();
    }

    @Test
    void testWaitForLock() {
    }

    @SneakyThrows
    @Test
    void testReleaseLock() {

        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.hasChangeLogLock()).isFalse();
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isNull();
        final boolean acquiredLock = cosmosLockService.acquireLock();
        assertThat(acquiredLock).isTrue();
        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isTrue();
        assertThat(cosmosLockService.hasChangeLogLock()).isTrue();
        final DatabaseChangeLogLock[] locks1 = cosmosLockService.listLocks();
        assertThat(locks1).hasSize(1);
        assertThat(((CosmosChangeLogLock)locks1[0]).getLocked()).isTrue();
        // Release lock
        cosmosLockService.releaseLock();
        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isTrue();
        assertThat(cosmosLockService.hasChangeLogLock()).isFalse();
        final DatabaseChangeLogLock[] locks2 = cosmosLockService.listLocks();
        assertThat(locks2).hasSize(0);
        //TODO: check locked=false row assertThat(((CosmosChangeLogLock)locks2[0]).getLocked()).isFalse();

        // Reacquire lock
        cosmosLockService.reset();
        final boolean reacquiredLock = cosmosLockService.acquireLock();
        assertThat(reacquiredLock).isTrue();
        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isTrue();
        assertThat(cosmosLockService.hasChangeLogLock()).isTrue();
        final DatabaseChangeLogLock[] locks3 = cosmosLockService.listLocks();
        assertThat(locks3).hasSize(1);
        assertThat(((CosmosChangeLogLock)locks3[0]).getLocked()).isTrue();
    }

    @SneakyThrows
    @Test
    void testListLocks() {
        cosmosLockService.init();
        final ChangeLogLockRepository repository = new ChangeLogLockRepository(cosmosDatabase, cosmosLockService.getDatabaseChangeLogLockTableName());

        final DatabaseChangeLogLock[] locks1 = cosmosLockService.listLocks();
        assertThat(locks1).hasSize(0);

        final CosmosChangeLogLock expectedChangeLogLock1 = CosmosChangeLogLock.builder()
                .id(1)
                .lockGranted(new Date())
                .lockedBy("me1")
                .locked(true)
                .build();

        repository.upsert(expectedChangeLogLock1);

        final DatabaseChangeLogLock[] locks2 = cosmosLockService.listLocks();
        assertThat(locks2).hasSize(1);

        final CosmosChangeLogLock expectedChangeLogLock2 = CosmosChangeLogLock.builder()
                .id(2)
                .lockGranted(new Date())
                .lockedBy("me2")
                .locked(true)
                .build();

        repository.upsert(expectedChangeLogLock2);

        final DatabaseChangeLogLock[] locks3 = cosmosLockService.listLocks();
        assertThat(locks3).hasSize(2);
    }

    @SneakyThrows
    @Test
    void testForceReleaseLock() {
        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.hasChangeLogLock()).isFalse();
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isNull();
        cosmosLockService.forceReleaseLock();
        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isTrue();
        assertThat(cosmosLockService.hasChangeLogLock()).isFalse();
        final DatabaseChangeLogLock[] locks1 = cosmosLockService.listLocks();
        assertThat(locks1).hasSize(0);
        //TODO: check locked=false row assertThat(((CosmosChangeLogLock)locks1[0]).getLocked()).isFalse();

        // Release lock on force released
        cosmosLockService.releaseLock();
        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isTrue();
        assertThat(cosmosLockService.hasChangeLogLock()).isFalse();
        final DatabaseChangeLogLock[] locks2 = cosmosLockService.listLocks();
        assertThat(locks2).hasSize(0);
        //TODO: check locked=false row assertThat(((CosmosChangeLogLock)locks2[0]).getLocked()).isFalse();

        // Acquire lock after released
        cosmosLockService.reset();
        final boolean reacquiredLock = cosmosLockService.acquireLock();
        assertThat(reacquiredLock).isTrue();
        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isTrue();
        assertThat(cosmosLockService.hasChangeLogLock()).isTrue();
        final DatabaseChangeLogLock[] locks3 = cosmosLockService.listLocks();
        assertThat(locks3).hasSize(1);
        assertThat(((CosmosChangeLogLock)locks3[0]).getLocked()).isTrue();
        // Force release after Acquired
        cosmosLockService.forceReleaseLock();
        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isTrue();
        assertThat(cosmosLockService.hasChangeLogLock()).isFalse();
        final DatabaseChangeLogLock[] locks4 = cosmosLockService.listLocks();
        assertThat(locks4).hasSize(0);
        //TODO: check locked=false row assertThat(((CosmosChangeLogLock)locks4[0]).getLocked()).isFalse();
    }

    @SneakyThrows
    @Test
    void testDestroy() {
        cosmosLockService.init();
        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.hasChangeLogLock()).isFalse();
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isTrue();
        assertThat(countContainersByNameStatement.queryForLong(database)).isEqualTo(1L);
        // Destroy
        cosmosLockService.destroy();
        assertThat(cosmosLockService.getExecutor()).isEqualTo(cosmosExecutor);
        assertThat(cosmosLockService.hasChangeLogLock()).isFalse();
        assertThat(cosmosLockService.getHasDatabaseChangeLogLockTable()).isNull();
        assertThat(countContainersByNameStatement.queryForLong(database)).isEqualTo(0L);

    }
}
