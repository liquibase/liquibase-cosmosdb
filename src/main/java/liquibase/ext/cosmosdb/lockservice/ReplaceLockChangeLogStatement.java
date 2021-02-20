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

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.ext.cosmosdb.database.CosmosLiquibaseDatabase;
import liquibase.ext.cosmosdb.statement.AbstractCosmosContainerStatement;
import liquibase.nosql.statement.NoSqlUpdateStatement;
import liquibase.util.NetUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Optional;

@Getter
@Setter
public class ReplaceLockChangeLogStatement extends AbstractCosmosContainerStatement
        implements NoSqlUpdateStatement<CosmosLiquibaseDatabase> {

    public static final String COMMAND_NAME = "replaceLock";

    protected static final String HOST_NAME;
    protected static final String HOST_ADDRESS;
    public static final String LIQUIBASE_HOST_DESCRIPTION = "liquibase.hostDescription";
    protected static final String HOST_DESCRIPTION
            = Optional.ofNullable(System.getProperty(LIQUIBASE_HOST_DESCRIPTION)).map("#"::concat).orElse("");

    static {
        try {
            HOST_NAME = NetUtil.getLocalHostName();
            HOST_ADDRESS = NetUtil.getLocalHostAddress();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    @Getter
    private final boolean locked;

    public ReplaceLockChangeLogStatement(final String containerName, final boolean locked) {
        super(containerName);
        this.locked = locked;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String toJs() {
        return "db." +
                getCommandName() +
                "(" +
                containerName +
                ", " + locked +
                ");";
    }

    protected int replace(final CosmosLiquibaseDatabase database) {
        final ChangeLogLockRepository repository = new ChangeLogLockRepository(database.getCosmosDatabase(), getContainerName());

        final CosmosChangeLogLock lockEntry = new CosmosChangeLogLock(ITEM_ID_1, new Date()
                , HOST_NAME + HOST_DESCRIPTION + " (" + HOST_ADDRESS + ")", locked);

        return repository.upsert(lockEntry);
    }

    @Override
    public int update(final CosmosLiquibaseDatabase database) {
       return replace(database);

    }
}
