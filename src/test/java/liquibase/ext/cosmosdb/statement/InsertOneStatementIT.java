package liquibase.ext.cosmosdb.statement;

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
import com.azure.cosmos.implementation.ConflictException;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import liquibase.ext.cosmosdb.AbstractCosmosWithConnectionIntegrationTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class InsertOneStatementIT extends AbstractCosmosWithConnectionIntegrationTest {

    @SneakyThrows
    @Test
    void testExecute() {

        final CreateContainerStatement createContainerStatement
                = new CreateContainerStatement(CONTAINER_NAME_PERSON, PARTITION_KEY_PATH_LAST_NAME);
        createContainerStatement.execute(database);

        final CosmosContainer cosmosContainer = cosmosDatabase.getContainer(CONTAINER_NAME_PERSON);

        assertThat(cosmosContainer).isNotNull();
        assertThat(cosmosContainer.getId()).isEqualTo(CONTAINER_NAME_PERSON);

        final CreateItemStatement createItemStatementId1Partition1
                = new CreateItemStatement(CONTAINER_NAME_PERSON, "{\"id\" : \"1\", \"lastName\" : \"LastName1\", \"firstName\" : \"FirstName1\", \"age\" : \"99\"}");
        createItemStatementId1Partition1.execute(database);

        final CreateItemStatement createItemStatementId1Partition2
                = new CreateItemStatement(CONTAINER_NAME_PERSON, "{\"id\" : \"1\", \"lastName\" : \"LastName2\", \"firstName\" : \"FirstName1\", \"age\" : \"99\"}");
        createItemStatementId1Partition2.execute(database);
        final CreateItemStatement createItemStatementId2Partition2
                = new CreateItemStatement(CONTAINER_NAME_PERSON, "{\"id\" : \"2\", \"lastName\" : \"LastName2\", \"firstName\" : \"FirstName2\", \"age\" : \"99\"}");
        createItemStatementId2Partition2.execute(database);

        final CreateItemStatement createItemStatementSameId1SamePartition1 = new CreateItemStatement(CONTAINER_NAME_PERSON, "{\"id\" : \"1\", \"lastName\" : \"LastName1\", \"firstName\" : \"FirstName2\", \"age\" : \"10\"}");
                assertThatExceptionOfType(ConflictException.class).isThrownBy(() -> createItemStatementSameId1SamePartition1.execute(database));

        CosmosItemResponse<Map> documentId1Partition1 = cosmosContainer.readItem("1", new PartitionKey("LastName1"), Map.class);
        assertThat(documentId1Partition1.getItem().get("firstName")).isEqualTo("FirstName1");

        assertThat(cosmosContainer.readAllItems(new PartitionKey("LastName1"), Map.class).stream().count()).isEqualTo(1);
        assertThat(cosmosContainer.readAllItems(new PartitionKey("LastName2"), Map.class).stream().count()).isEqualTo(2);

    }
}
