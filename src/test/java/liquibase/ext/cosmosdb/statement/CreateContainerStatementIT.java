package liquibase.ext.cosmosdb.statement;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.PartitionKind;
import liquibase.ext.cosmosdb.AbstractCosmosWithConnectionIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static liquibase.ext.cosmosdb.statement.JsonUtils.DEFAULT_PARTITION_KEY_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CreateContainerStatementIT extends AbstractCosmosWithConnectionIntegrationTest {

    public static final String CONTAINER_NAME_1 = "containerName1";
    public static final String PARTITION_KEY_PATH_1 = "{ \"partitionKey\": {\"paths\": [\"/partitionField1\"], \"kind\": \"Hash\" } }";
    public static final String PARTITION_KEY_NO_KIND = "{ \"partitionKey\": {\"paths\": [\"/partitionField1\"] } }";
    public static final String PARTITION_KEY_MULTI_NO_KIND = "{ \"partitionKey\": {\"paths\": [\"/tenantId\", \"/userId\"] } }";
    public static final String PROPERTIES_NO_PARTITION_KEY =
            "{ \"indexingPolicy\": { \"indexingMode\": \"consistent\", \"automatic\": true,"
                    + " \"includedPaths\": [{\"path\": \"/*\"}], \"excludedPaths\": [] } }";


    @Test
    void testExecute() {
        final CreateContainerStatement createContainerStatement
                = new CreateContainerStatement(CONTAINER_NAME_1, PARTITION_KEY_PATH_1);

        createContainerStatement.execute(database);

        final CosmosContainer cosmosContainer = cosmosDatabase.getContainer(CONTAINER_NAME_1);

        assertThat(cosmosContainer).isNotNull();
        assertThat(cosmosContainer.read().getProperties().getId()).isEqualTo(CONTAINER_NAME_1);

        // should fail if tried once more
        assertThatExceptionOfType(CosmosException.class).isThrownBy(() -> createContainerStatement.execute(database));

        assertThat(cosmosDatabase.getContainer("testcoll")).isNotNull();

    }

    @Test
    void testExecuteWithThroughput() {
        CreateContainerStatement createContainerStatement
                = new CreateContainerStatement("container_manual", PARTITION_KEY_PATH_1, "500");

        createContainerStatement.execute(database);

        CosmosContainer cosmosContainer = cosmosDatabase.getContainer("container_manual");

        assertThat(cosmosContainer).isNotNull();
        assertThat(cosmosContainer.read().getProperties().getId()).isEqualTo("container_manual");
        assertThat(cosmosContainer.readThroughput().getProperties().getManualThroughput()).isEqualTo(500);

        // AutoscaleMaxThroughput

        createContainerStatement
                = new CreateContainerStatement("container_auto", PARTITION_KEY_PATH_1, "{\"maxThroughput\": 8000}");

        createContainerStatement.execute(database);

        cosmosContainer = cosmosDatabase.getContainer("container_auto");

        assertThat(cosmosContainer).isNotNull();
        assertThat(cosmosContainer.read().getProperties().getId()).isEqualTo("container_auto");
        assertThat(cosmosContainer.readThroughput().getProperties().getAutoscaleMaxThroughput()).isEqualTo(8000);

    }

    @Test
    void testExecuteDefaultsSinglePathKindToHash() {
        new CreateContainerStatement("container_single_no_kind", PARTITION_KEY_NO_KIND).execute(database);

        final CosmosContainer cosmosContainer = cosmosDatabase.getContainer("container_single_no_kind");
        assertThat(cosmosContainer.read().getProperties().getPartitionKeyDefinition())
                .isNotNull()
                .returns(PartitionKind.HASH, pk -> pk.getKind())
                .satisfies(pk -> assertThat(pk.getPaths()).containsExactly("/partitionField1"));
    }

    @Tag("cosmos-supports-multihash-partition-keys")
    @Test
    void testExecuteDefaultsMultiPathKindToMultiHash() {
        new CreateContainerStatement("container_multi_no_kind", PARTITION_KEY_MULTI_NO_KIND).execute(database);

        final CosmosContainer cosmosContainer = cosmosDatabase.getContainer("container_multi_no_kind");
        assertThat(cosmosContainer.read().getProperties().getPartitionKeyDefinition())
                .isNotNull()
                .returns(PartitionKind.MULTI_HASH, pk -> pk.getKind())
                .satisfies(pk -> assertThat(pk.getPaths()).containsExactly("/tenantId", "/userId"));
    }

    @Test
    void testExecuteKeepsDefaultPathWhenPartitionKeyOmitted() {
        new CreateContainerStatement("container_no_pk", PROPERTIES_NO_PARTITION_KEY).execute(database);

        final CosmosContainer cosmosContainer = cosmosDatabase.getContainer("container_no_pk");
        assertThat(cosmosContainer.read().getProperties().getPartitionKeyDefinition())
                .isNotNull()
                .returns(PartitionKind.HASH, pk -> pk.getKind())
                .satisfies(pk -> assertThat(pk.getPaths()).containsExactly(DEFAULT_PARTITION_KEY_PATH));
    }
}
