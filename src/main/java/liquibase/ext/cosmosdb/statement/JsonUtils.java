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
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import lombok.NoArgsConstructor;

import java.util.Arrays;

import static com.azure.cosmos.implementation.Constants.Properties.AUTOPILOT_MAX_THROUGHPUT;
import static com.azure.cosmos.implementation.Constants.Properties.ID;
import static com.azure.cosmos.implementation.Constants.Properties.PATH_SEPARATOR;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static liquibase.util.StringUtil.isNotEmpty;
import static liquibase.util.StringUtil.trimToNull;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class JsonUtils {
    public static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();
    public static final String DEFAULT_PARTITION_KEY_NAME = "null";
    public static final String DEFAULT_PARTITION_KEY_PATH = PATH_SEPARATOR + DEFAULT_PARTITION_KEY_NAME;
    public static final String COSMOS_ID_PARAMETER = "@" + ID;
    public static final String QUERY_SELECT_ALL = "SELECT * FROM c";

    public static Document orEmptyDocument(final String json) {
        return ofNullable(trimToNull(json)).map(Document::new)
                .orElseGet(Document::new);
    }

    /**
     * Deserialize the json to query parameters.
     *
     * @param json the query parameters in json format.
     *             See request body: https://docs.microsoft.com/en-us/rest/api/cosmos-db/query-documents.
     * @return the {@link SqlQuerySpec}.
     */
    public static SqlQuerySpec orEmptySqlQuerySpec(final String json) {

        return ofNullable(trimToNull(json)).map(JsonSerializable::new)
                .map(js -> {
                    final SqlQuerySpec querySpec = new SqlQuerySpec();
                    querySpec.setQueryText(js.getString("query"));
                    querySpec.setParameters(js.getList("parameters", SqlParameter.class));
                    return querySpec;
                }).orElseGet(SqlQuerySpec::new);
    }

    /**
     * Deserialize the json to Stored Procedure parameters.
     *
     * @param json the Stored Procedure in json format.
     *             See request body: https://docs.microsoft.com/en-us/rest/api/cosmos-db/create-a-stored-procedure.
     * @return the {@link CosmosStoredProcedureProperties}.
     */
    public static CosmosStoredProcedureProperties orEmptyStoredProcedureProperties(final String json) {

        return ofNullable(trimToNull(json)).map(JsonSerializable::new)
                .map(js -> new CosmosStoredProcedureProperties(
                        js.getString("id"),
                        js.getString("body"))).orElse(new CosmosStoredProcedureProperties(null, null));
    }

    public static Document mergeDocuments(final Document destination, final Document source) {
        destination.getPropertyBag().setAll(source.getPropertyBag().deepCopy());
        return destination;
    }

    public static CosmosContainerProperties toContainerProperties(final String containerId, final String containerPropertiesJson) {

        final CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(containerId, DEFAULT_PARTITION_KEY_PATH);
        if (isNotEmpty(trimToNull(containerPropertiesJson))) {
            final DocumentCollection documentCollection = new DocumentCollection(containerPropertiesJson);
            if (nonNull(documentCollection.getPartitionKey())) {
                cosmosContainerProperties.setPartitionKeyDefinition(documentCollection.getPartitionKey());
            }
            if (nonNull(documentCollection.getIndexingPolicy())) {
                cosmosContainerProperties.setIndexingPolicy(documentCollection.getIndexingPolicy());
            }
            if (nonNull(documentCollection.getUniqueKeyPolicy())) {
                cosmosContainerProperties.setUniqueKeyPolicy(documentCollection.getUniqueKeyPolicy());
            }
            if (nonNull(documentCollection.getAnalyticalStoreTimeToLiveInSeconds())) {
                cosmosContainerProperties.setAnalyticalStoreTimeToLiveInSeconds(documentCollection.getAnalyticalStoreTimeToLiveInSeconds());
            }
            if (nonNull(documentCollection.getDefaultTimeToLive())) {
                cosmosContainerProperties.setDefaultTimeToLiveInSeconds(documentCollection.getDefaultTimeToLive());
            }
            if (nonNull(documentCollection.getConflictResolutionPolicy())) {
                cosmosContainerProperties.setConflictResolutionPolicy(documentCollection.getConflictResolutionPolicy());
            }
        }
        return cosmosContainerProperties;
    }

    public static ThroughputProperties toThroughputProperties(final String throughputPropertiesJson) {

        if (nonNull(trimToNull(throughputPropertiesJson))) {
            final TreeNode node;
            try {
                node = OBJECT_MAPPER.readTree(throughputPropertiesJson);
            } catch (final JsonProcessingException e) {
                throw new IllegalArgumentException(String.format("Unable to parse JSON %s", throughputPropertiesJson), e);
            }
            if (node.isValueNode()) {
                return ThroughputProperties.createManualThroughput(((ValueNode) node).asInt());
            }
            if (node.isContainerNode() && ((ContainerNode<?>) node).has(AUTOPILOT_MAX_THROUGHPUT)) {
                return ThroughputProperties.createAutoscaledThroughput(((ValueNode) node.get(AUTOPILOT_MAX_THROUGHPUT)).asInt());
            }
        }
        return null;
    }

    public static String extractPartitionKeyPath(final CosmosContainer cosmosContainer) {
        final CosmosContainerProperties cosmosContainerProperties = cosmosContainer.read().getProperties();
        return cosmosContainerProperties.getPartitionKeyDefinition().getPaths().stream().findFirst().orElse(DEFAULT_PARTITION_KEY_PATH);
    }

    public static PartitionKey extractPartitionKeyByPath(final Document document, final String path) {
        return ofNullable(
                document.getObjectByPath(Arrays.asList(path.replaceFirst("/", "").split("/")))
        ).map(PartitionKey::new).orElse(PartitionKey.NONE);
    }

    public static PartitionKey extractPartitionKey(final Document document, final CosmosContainer cosmosContainer) {
        return extractPartitionKeyByPath(document,
                extractPartitionKeyPath(cosmosContainer));
    }
}
