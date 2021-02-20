package liquibase.ext.cosmosdb.statement;

import liquibase.ext.cosmosdb.AbstractCosmosWithConnectionIntegrationTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeleteAllContainersStatementIT extends AbstractCosmosWithConnectionIntegrationTest {

    private static final String CONTAINER_NAME_1 = "containerName1";
    private static final String CONTAINER_NAME_2 = "containerName2";

    @SneakyThrows
    @Test
    void testExecute() {

        final CreateContainerStatement createContainerStatement1
                = new CreateContainerStatement(CONTAINER_NAME_1);

        final CreateContainerStatement createContainerStatement2
                = new CreateContainerStatement(CONTAINER_NAME_2);

        createContainerStatement1.execute(database);
        createContainerStatement2.execute(database);

        final Long countAfterCreated = cosmosDatabase.readAllContainers().stream().count();
        assertThat(countAfterCreated).isEqualTo(2L);

        final DeleteAllContainersStatement deleteAllContainersStatement
                = new DeleteAllContainersStatement();

        deleteAllContainersStatement.execute(database);

        final Long countAfterDeleted = cosmosDatabase.readAllContainers().stream().count();
        assertThat(countAfterDeleted).isEqualTo(0L);

    }
}