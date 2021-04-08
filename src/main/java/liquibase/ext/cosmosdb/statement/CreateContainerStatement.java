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
import liquibase.nosql.statement.NoSqlExecuteStatement;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static java.lang.Boolean.FALSE;
import static java.util.Optional.ofNullable;
import static liquibase.ext.cosmosdb.statement.JsonUtils.toContainerProperties;
import static liquibase.ext.cosmosdb.statement.JsonUtils.toThroughputProperties;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
public class CreateContainerStatement extends AbstractCosmosStatement
        implements NoSqlExecuteStatement<CosmosLiquibaseDatabase> {

    public static final String COMMAND_NAME = "createContainer";

    private final String containerId;
    private final String containerProperties;
    private final String throughputProperties;
    private final Boolean skipExisting;

    public CreateContainerStatement(final String containerId, final String containerProperties, final String throughputProperties) {
        this(containerId, containerProperties, throughputProperties, FALSE);
    }

    public CreateContainerStatement(final String containerId, final String containerProperties) {
        this(containerId, containerProperties, null);
    }

    public CreateContainerStatement(final String containerId) {
        this(containerId, null);
    }

    public CreateContainerStatement() {
        this(null);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String toJs() {
        return
                "db."
                        + getCommandName()
                        + "("
                        + getContainerId()
                        + ", "
                        + getContainerProperties()
                        + ", "
                        + getThroughputProperties()
                        + ", "
                        + getSkipExisting()
                        + ");";
    }

    @Override
    public void execute(final CosmosLiquibaseDatabase database) {
        final CosmosContainerProperties cosmosContainerProperties = toContainerProperties(getContainerId(), getContainerProperties());
        final ThroughputProperties cosmosThroughputProperties = toThroughputProperties(getThroughputProperties());
        if (ofNullable(skipExisting).orElse(FALSE)) {
            database.getCosmosDatabase().createContainerIfNotExists(cosmosContainerProperties, cosmosThroughputProperties);
        } else {
            database.getCosmosDatabase().createContainer(cosmosContainerProperties, cosmosThroughputProperties);
        }
    }

}
