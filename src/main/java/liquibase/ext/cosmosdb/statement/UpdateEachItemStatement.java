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
import com.azure.cosmos.models.SqlQuerySpec;
import liquibase.ext.cosmosdb.database.CosmosLiquibaseDatabase;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static liquibase.ext.cosmosdb.statement.JsonUtils.orEmptySqlQuerySpec;

public class UpdateEachItemStatement extends CreateItemStatement {

    public static final String COMMAND_NAME = "updateEachItem";

    private final SqlQuerySpec query;

    public UpdateEachItemStatement(final String containerName, final String jsonQuery, final String jsonDocument) {
        super(containerName, jsonDocument);
        this.query = orEmptySqlQuerySpec(jsonQuery);
    }

    public UpdateEachItemStatement(final String containerName, final SqlQuerySpec query, final Document document) {
        super(containerName, document);
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
        final CosmosContainer cosmosContainer = database.getCosmosDatabase().getContainer(containerName);

        final Document source = getDocument();

        final List<Document> documents = cosmosContainer
                .queryItems(query, null, Map.class).stream().map(JsonUtils::fromMap)
                .map(Document.class::cast).map(d -> JsonUtils.mergeDocuments(d, source))
                .collect(Collectors.toList());

        documents.forEach(destination -> {
            JsonUtils.mergeDocuments(source, destination);
            cosmosContainer.upsertItem(destination);
        });
    }

}
