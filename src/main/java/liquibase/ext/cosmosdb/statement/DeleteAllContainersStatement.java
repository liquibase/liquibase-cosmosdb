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
import com.azure.cosmos.models.CosmosContainerProperties;
import liquibase.ext.cosmosdb.database.CosmosLiquibaseDatabase;
import liquibase.nosql.statement.NoSqlExecuteStatement;

import java.util.Collections;
import java.util.List;

public class DeleteAllContainersStatement extends AbstractCosmosStatement
        implements NoSqlExecuteStatement<CosmosLiquibaseDatabase> {

    public static final String COMMAND_NAME = "deleteAllContainers";

    final List<String> ignoreContainerNames;

    public DeleteAllContainersStatement() {
        this(Collections.emptyList());
    }

    public DeleteAllContainersStatement(final List<String> ignoreContainerNames) {
        this.ignoreContainerNames = ignoreContainerNames;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String toJs() {
        return
                "db." +
                        getCommandName() +
                        "(" +
                        ignoreContainerNames.toString() +
                        ");";
    }

    @Override
    public void execute(final CosmosLiquibaseDatabase database) {
        database.getCosmosDatabase().readAllContainers().stream()
                .map(CosmosContainerProperties::getId).filter(id -> !ignoreContainerNames.contains(id))
                .map((id) -> database.getCosmosDatabase().getContainer(id)).forEach(CosmosContainer::delete);
    }

}
