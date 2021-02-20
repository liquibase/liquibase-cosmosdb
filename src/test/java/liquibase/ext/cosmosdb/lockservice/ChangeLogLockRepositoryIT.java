package liquibase.ext.cosmosdb.lockservice;

import com.azure.cosmos.CosmosContainer;
import liquibase.ext.cosmosdb.AbstractCosmosWithConnectionIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ChangeLogLockRepositoryIT extends AbstractCosmosWithConnectionIntegrationTest {

    public static final String CONTAINER_NAME_1 = "containerName1Lock";

    public ChangeLogLockRepository repository;
    public CosmosContainer container;

    @BeforeEach
    protected void setUpEach() {
        super.setUpEach();
        new CreateChangeLogLockContainerStatement(CONTAINER_NAME_1).execute(database);
        repository = new ChangeLogLockRepository(cosmosDatabase, CONTAINER_NAME_1);
        container = repository.getContainer();
    }

    @Test
    void testGet() {

        final CosmosChangeLogLock expectedChangeLogLock = CosmosChangeLogLock.builder()
                .id(1)
                .lockGranted(new Date())
                .lockedBy("me")
                .locked(true)
                .build();

        repository.upsert(expectedChangeLogLock);

        final Optional<CosmosChangeLogLock> actualChangeLogLock = repository.get("1");

        assertThat(actualChangeLogLock).isNotEmpty();
        assertThat(actualChangeLogLock.get()).isEqualTo(expectedChangeLogLock);
    }

    @Test
    void testGetAll() {

        assertThat(repository.getAll()).hasSize(0);

        final CosmosChangeLogLock expectedChangeLogLock1 = CosmosChangeLogLock.builder()
                .id(1)
                .lockGranted(new Date())
                .lockedBy("me")
                .locked(true)
                .build();

        repository.upsert(expectedChangeLogLock1);

        assertThat(repository.getAll()).hasSize(1);

        final CosmosChangeLogLock expectedChangeLogLock2 = CosmosChangeLogLock.builder()
                .id(2)
                .lockGranted(new Date())
                .lockedBy("me2")
                .locked(true)
                .build();

        repository.upsert(expectedChangeLogLock2);

        assertThat(repository.getAll()).hasSize(2);

    }

    @Test
    void testSave() {

        final CosmosChangeLogLock expectedChangeLogLock1 = CosmosChangeLogLock.builder()
                .id(1)
                .lockGranted(new Date())
                .lockedBy("me")
                .locked(true)
                .build();

        repository.upsert(expectedChangeLogLock1);

        final Optional<CosmosChangeLogLock> actualChangeLogLock1 = repository.get("1");

        assertThat(actualChangeLogLock1).isNotEmpty();
        assertThat(actualChangeLogLock1.get()).isEqualTo(expectedChangeLogLock1);

        // Overwrite
        final CosmosChangeLogLock expectedChangeLogLock2 = CosmosChangeLogLock.builder()
                .id(1)
                .lockGranted(new Date())
                .lockedBy("me2")
                .locked(true)
                .build();

        repository.upsert(expectedChangeLogLock2);

        final Optional<CosmosChangeLogLock> actualChangeLogLock2 = repository.get("1");

        assertThat(actualChangeLogLock2).isNotEmpty();
        assertThat(actualChangeLogLock2.get()).isEqualTo(expectedChangeLogLock2);

    }

    @Test
    void testToDate() {
    }

    @Test
    void testFromDate() {
    }

    @Test
    void testExists() {

    assertThat(repository.exists("1")).isFalse();

        final CosmosChangeLogLock expectedChangeLogLock1 = CosmosChangeLogLock.builder()
                .id(1)
                .lockGranted(new Date())
                .lockedBy("me2")
                .locked(true)
                .build();

        repository.upsert(expectedChangeLogLock1);

        assertThat(repository.exists("2")).isFalse();
        assertThat(repository.exists("1")).isTrue();
    }

    @Test
    void testFromDocument() {
    }

    @Test
    void testToDocument() {
    }
}