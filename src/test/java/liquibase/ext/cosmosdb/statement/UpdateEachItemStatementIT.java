package liquibase.ext.cosmosdb.statement;

import liquibase.ext.cosmosdb.AbstractCosmosWithConnectionIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateEachItemStatementIT extends AbstractCosmosWithConnectionIntegrationTest {

    @Test
    @SuppressWarnings("unchecked")
    void testExecute() {

        final CreateContainerStatement createContainerStatement
                = new CreateContainerStatement(CONTAINER_NAME_PERSON, PARTITION_KEY_PATH_LAST_NAME);
        createContainerStatement.execute(database);

        CreateItemStatement createItemStatementId1Partition1
                = new CreateItemStatement(CONTAINER_NAME_PERSON, "{\"id\" : \"1\", \"lastName\" : \"LastNameRemained1\", \"firstName\" : \"FirstNameShouldBeChanged1\"}");
        createItemStatementId1Partition1.execute(database);

        createItemStatementId1Partition1
                = new CreateItemStatement(CONTAINER_NAME_PERSON, "{\"id\" : \"2\", \"lastName\" : \"LastNameRemained2\", \"firstName\" : \"FirstNameShouldRemain2\"}");
        createItemStatementId1Partition1.execute(database);

        Optional<Map<String, Object>> actual = cosmosDatabase.getContainer(CONTAINER_NAME_PERSON).queryItems("SELECT * FROM " + CONTAINER_NAME_PERSON + " f WHERE f.id = \"1\"", null, Map.class)
                .stream().map(i -> (Map<String, Object>) i).findFirst();

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .hasFieldOrPropertyWithValue("id", "1")
                .hasFieldOrPropertyWithValue("lastName", "LastNameRemained1")
                .hasFieldOrPropertyWithValue("firstName", "FirstNameShouldBeChanged1")
                .doesNotContainKey("age");

        String jsonQuery = "{  \n" +
                "  \"query\": \"SELECT * FROM " + CONTAINER_NAME_PERSON + " f WHERE f.id = @id\",  \n" +
                "  \"parameters\": [  \n" +
                "    {  \n" +
                "      \"name\": \"@id\",  \n" +
                "      \"value\": \"1\"  \n" +
                "    }  \n" +
                "  ]  \n" +
                "}  ";

        final UpdateEachItemStatement updateEachItemStatement = new UpdateEachItemStatement(CONTAINER_NAME_PERSON, jsonQuery, "{\"id\" : \"1\", \"firstName\" : \"FirstNameUpdated\", \"age\" : \"100\"}");
        updateEachItemStatement.execute(database);

        actual = cosmosDatabase.getContainer(CONTAINER_NAME_PERSON).queryItems("SELECT * FROM " + CONTAINER_NAME_PERSON + " f WHERE f.id = \"1\"", null, Map.class)
                .stream().map(i -> (Map<String, Object>) i).findFirst();

        assertThat(actual).isPresent();
        assertThat((Map<?, ?>) actual.get())
                .hasFieldOrPropertyWithValue("id", "1")
                .hasFieldOrPropertyWithValue("lastName", "LastNameRemained1")
                .hasFieldOrPropertyWithValue("firstName", "FirstNameUpdated")
                .hasFieldOrPropertyWithValue("age", "100");

        actual = cosmosDatabase.getContainer(CONTAINER_NAME_PERSON).queryItems("SELECT * FROM " + CONTAINER_NAME_PERSON + " f WHERE f.id = \"2\"", null, Map.class)
                .stream().map(i -> (Map<String, Object>) i).findFirst();
        assertThat(actual).isPresent();
        assertThat(actual.get())
                .hasFieldOrPropertyWithValue("id", "2")
                .hasFieldOrPropertyWithValue("lastName", "LastNameRemained2")
                .hasFieldOrPropertyWithValue("firstName", "FirstNameShouldRemain2")
                .doesNotContainKey("age");
    }
}