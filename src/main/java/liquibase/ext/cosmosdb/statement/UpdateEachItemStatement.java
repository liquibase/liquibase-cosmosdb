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
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.fasterxml.jackson.databind.node.ObjectNode;
import liquibase.ext.cosmosdb.database.CosmosLiquibaseDatabase;

import static liquibase.ext.cosmosdb.statement.JsonUtils.extractPartitionKeyByPath;
import static liquibase.ext.cosmosdb.statement.JsonUtils.extractPartitionKeyPath;
import static liquibase.ext.cosmosdb.statement.JsonUtils.mergeDocuments;
import static liquibase.ext.cosmosdb.statement.JsonUtils.orEmptySqlQuerySpec;

public class UpdateEachItemStatement extends CreateItemStatement {

    public static final String COMMAND_NAME = "updateEachItem";

    private final SqlQuerySpec query;

    public UpdateEachItemStatement(final String containerId, final String jsonQuery, final String jsonDocument) {
        super(containerId, jsonDocument);
        this.query = orEmptySqlQuerySpec(jsonQuery);
    }

    public UpdateEachItemStatement(final String containerId, final SqlQuerySpec query, final Document document) {
        super(containerId, document);
        this.query = query;
    }

    public UpdateEachItemStatement() {
        this(null, (SqlQuerySpec) null, null);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public void execute(final CosmosLiquibaseDatabase database) {
        final CosmosContainer cosmosContainer = database.getCosmosDatabase().getContainer(containerId);
        final String partitionKeyPath = extractPartitionKeyPath(cosmosContainer);

        final Document source = getDocument();

        cosmosContainer
                .queryItems(query, null, ObjectNode.class).stream().map(Document::new)
                .map(d -> mergeDocuments(d, source))
                .forEach(destination -> {
            final PartitionKey partitionKey = extractPartitionKeyByPath(destination, partitionKeyPath);
            cosmosContainer.upsertItem(destination, partitionKey, null);
        });
    }

}
