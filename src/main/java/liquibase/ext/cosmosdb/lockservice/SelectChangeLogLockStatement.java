package liquibase.ext.cosmosdb.lockservice;

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

import liquibase.ext.cosmosdb.database.CosmosLiquibaseDatabase;
import liquibase.ext.cosmosdb.statement.AbstractCosmosContainerStatement;
import liquibase.nosql.statement.NoSqlQueryForObjectStatement;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class SelectChangeLogLockStatement extends AbstractCosmosContainerStatement
        implements NoSqlQueryForObjectStatement<CosmosLiquibaseDatabase> {

    public static final String COMMAND_NAME = "readLock";

    public SelectChangeLogLockStatement(final String containerName) {
        super(containerName);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    public Optional<CosmosChangeLogLock> read(final CosmosLiquibaseDatabase database) {
        final ChangeLogLockRepository repository =
                new ChangeLogLockRepository(database.getCosmosDatabase(), getContainerName());
        return repository.get(ITEM_ID_1_STRING);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> T queryForObject(final CosmosLiquibaseDatabase database, final Class<T> requiredType) {
        return (T) read(database).orElse(null);
    }
}
