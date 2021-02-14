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

import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.ThroughputProperties;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.lang.Boolean.FALSE;
import static java.util.Optional.ofNullable;
import static liquibase.ext.cosmosdb.statement.JsonUtils.toContainerProperties;
import static liquibase.ext.cosmosdb.statement.JsonUtils.toThroughputProperties;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
public class CreateContainerStatement extends AbstractNoSqlStatement implements NoSqlExecuteStatement {

    public static final String COMMAND_NAME = "createContainer";

    private String containerName;
    private String options;
    private String throughput;
    private Boolean skipExisting;

    public CreateContainerStatement(final String containerName, final String options, final String throughput) {
        this(containerName, options, throughput, FALSE);
    }

    public CreateContainerStatement(final String containerName, final String options) {
        this(containerName, options, null);
    }

    public CreateContainerStatement(final String containerName) {
        this(containerName, null);
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
                        + getContainerName()
                        + ", "
                        + getOptions()
                        + ", "
                        + getThroughput()
                        + ", "
                        + getSkipExisting()
                        + ");";
    }

    @Override
    public void execute(final CosmosDatabase cosmosDatabase) {
        final CosmosContainerProperties cosmosContainerProperties = toContainerProperties(getContainerName(), getOptions());
        final ThroughputProperties throughputProperties = toThroughputProperties(getThroughput());
        if (ofNullable(skipExisting).orElse(FALSE)) {
            cosmosDatabase.createContainerIfNotExists(cosmosContainerProperties, throughputProperties);
        } else {
            cosmosDatabase.createContainer(cosmosContainerProperties, throughputProperties);
        }
    }

}
