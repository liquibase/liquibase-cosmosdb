package liquibase.ext.cosmosdb.lockservice;

import liquibase.database.core.PostgresDatabase;
import liquibase.ext.cosmosdb.database.CosmosLiquibaseDatabase;
import liquibase.lockservice.LockServiceFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CosmosLockServiceTest {

    @Test
    void getPriority() {
        assertThat(new CosmosLockService().getPriority()).isEqualTo(10);
    }

    @Test
    void testSupports() {
        final CosmosLockService cosmosLockService = new CosmosLockService();
        assertThat(cosmosLockService.supports(new CosmosLiquibaseDatabase())).isTrue();
        assertThat(cosmosLockService.supports(new PostgresDatabase())).isFalse();
    }

    @Test
    void testGetDatabase() {
        final CosmosLiquibaseDatabase database = new CosmosLiquibaseDatabase();
        final CosmosLockService cosmosLockService = (CosmosLockService) LockServiceFactory.getInstance().getLockService(database);
        assertThat(cosmosLockService).isInstanceOf(CosmosLockService.class);
        assertThat(cosmosLockService.getDatabase()).isInstanceOf(CosmosLiquibaseDatabase.class);
        assertThat(cosmosLockService.getDatabase()).isEqualTo(database);
    }

    @Test
    void testGetDatabaseChangeLogLockTableName() {
        final CosmosLiquibaseDatabase database = new CosmosLiquibaseDatabase();
        final CosmosLockService cosmosLockService = (CosmosLockService) LockServiceFactory.getInstance().getLockService(database);
        assertThat(cosmosLockService.getDatabaseChangeLogLockTableName()).isEqualTo(database.getDatabaseChangeLogLockTableName());
    }

}