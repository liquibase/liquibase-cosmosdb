package liquibase.ext.cosmosdb.lockservice;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import liquibase.ext.cosmosdb.AbstractCosmosWithConnectionIntegrationTest;
import liquibase.ext.cosmosdb.statement.CreateContainerStatement;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CreateChangeLogLockContainerStatementIT extends AbstractCosmosWithConnectionIntegrationTest {

    public static final String CONTAINER_NAME_1 = "containerName1Lock";

    @SneakyThrows
    @Test
    void testExecute() {
        CreateContainerStatement createContainerStatement
                = new CreateChangeLogLockContainerStatement(CONTAINER_NAME_1);

        createContainerStatement.execute(database);

        CosmosContainer cosmosContainer = cosmosDatabase.getContainer(CONTAINER_NAME_1);

        assertThat(cosmosContainer).isNotNull();
        assertThat(cosmosContainer.getId()).isEqualTo(CONTAINER_NAME_1);
        //TODO: assert partition
        //assertThat(cosmosContainer.get???).isEqualTo(PARTITION_KEY_PATH_DEFAULT);

        assertThatExceptionOfType(CosmosException.class).isThrownBy(() -> createContainerStatement.execute(database));

    }
}