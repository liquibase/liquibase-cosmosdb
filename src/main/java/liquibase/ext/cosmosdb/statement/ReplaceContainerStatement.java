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

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.ThroughputProperties;
import liquibase.ext.cosmosdb.database.CosmosLiquibaseDatabase;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static java.util.Objects.nonNull;
import static liquibase.ext.cosmosdb.statement.JsonUtils.toContainerProperties;
import static liquibase.ext.cosmosdb.statement.JsonUtils.toThroughputProperties;
import static liquibase.util.StringUtil.trimToNull;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
public class ReplaceContainerStatement extends CreateContainerStatement {

    public static final String COMMAND_NAME = "replaceContainer";

    public ReplaceContainerStatement(final String containerId, final String containerProperties, final String throughputProperties) {
        super(containerId, containerProperties, throughputProperties);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public void execute(final CosmosLiquibaseDatabase database) {
        if (nonNull(trimToNull(getContainerProperties()))) {
            final CosmosContainerProperties cosmosContainerProperties = toContainerProperties(getContainerId(), getContainerProperties());
            database.getCosmosDatabase().getContainer(getContainerId()).replace(cosmosContainerProperties);
        }
        if (nonNull(trimToNull(getThroughputProperties()))) {
            final ThroughputProperties cosmosContainerProperties = toThroughputProperties(getThroughputProperties());
            database.getCosmosDatabase().getContainer(getContainerId()).replaceThroughput(cosmosContainerProperties);
        }
    }

}
