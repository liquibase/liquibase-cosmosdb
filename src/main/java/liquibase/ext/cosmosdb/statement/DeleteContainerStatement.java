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
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Getter
@EqualsAndHashCode(callSuper = true)
public class DeleteContainerStatement extends AbstractNoSqlContainerStatement implements NoSqlExecuteStatement {

    public static final String COMMAND_NAME = "delete";

    private final Boolean skipMissing;

    public DeleteContainerStatement(final String containerName, final Boolean skipMissing) {
        super(containerName);
        this.skipMissing = skipMissing;
    }

    public DeleteContainerStatement(final String containerName) {
        this(containerName, FALSE);
    }

    public DeleteContainerStatement() {
        this(null);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String toJs() {
        return
                "db." +
                        getContainerName() +
                        "." +
                        getCommandName() +
                        "(" +
                        skipMissing +
                        ");";
    }

    @Override
    public void execute(final CosmosDatabase cosmosDatabase) {
        if (TRUE.equals(skipMissing) && cosmosDatabase.readAllContainers()
                .stream().map(CosmosContainerProperties::getId).noneMatch(c -> c.equals(containerName))) {
            return;
        }

        final CosmosContainer cosmosContainer = cosmosDatabase.getContainer(containerName);
        cosmosContainer.delete();
    }

    @Override
    public String toString() {
        return this.toJs();
    }
}
