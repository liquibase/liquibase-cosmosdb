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
import com.azure.cosmos.CosmosException;
import liquibase.ext.cosmosdb.AbstractCosmosWithConnectionIntegrationTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;

class CountDocumentsInContainerStatementIT extends AbstractCosmosWithConnectionIntegrationTest {
    public static final String CONTAINER_NAME_PERSON = "person";

    @SneakyThrows
    @Test
    void testQueryForLong() {

        final CountDocumentsInContainerStatement countDocumentsInContainerStatement = new CountDocumentsInContainerStatement(CONTAINER_NAME_PERSON);
        assertThatExceptionOfType(CosmosException.class).isThrownBy(() -> countDocumentsInContainerStatement.queryForLong(database));

        final CreateContainerStatement createContainerStatement
                = new CreateContainerStatement(CONTAINER_NAME_PERSON, "{ \"partitionKey\": { \"paths\": [\"/partition\"] } }");
        createContainerStatement.execute(database);
        assertThat(countDocumentsInContainerStatement.queryForLong(database)).isEqualTo(0L);

        final CosmosContainer cosmosContainer = cosmosDatabase.getContainer(CONTAINER_NAME_PERSON);

        final CreateItemStatement createItemStatementId1NoPartition
                = new CreateItemStatement(CONTAINER_NAME_PERSON, "{\"id\" : \"1\", \"partition\" : \"LastName1\", \"firstName\" : \"FirstName1\", \"age\" : \"99\"}");
        createItemStatementId1NoPartition.execute(database);

        final CreateItemStatement createItemStatementId1PartitionDefault
                = new CreateItemStatement(CONTAINER_NAME_PERSON, "{\"id\" : \"1\", \"partition\" : \"default\", \"firstName\" : \"FirstName1\", \"age\" : \"99\"}");
        createItemStatementId1PartitionDefault.execute(database);
        final CreateItemStatement createItemStatementId2PartitionDefault
                = new CreateItemStatement(CONTAINER_NAME_PERSON, "{\"id\" : \"2\", \"partition\" : \"default\", \"firstName\" : \"FirstName2\", \"age\" : \"99\"}");
        createItemStatementId2PartitionDefault.execute(database);
        assertThat(countDocumentsInContainerStatement.queryForLong(database)).isEqualTo(2L);
    }
}
