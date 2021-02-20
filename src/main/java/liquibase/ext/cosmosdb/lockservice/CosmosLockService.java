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

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.ext.cosmosdb.database.CosmosLiquibaseDatabase;
import liquibase.ext.cosmosdb.statement.CountContainersByNameStatement;
import liquibase.ext.cosmosdb.statement.DeleteContainerStatement;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.logging.Logger;
import liquibase.nosql.lockservice.AbstractNoSqlLockService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;

public class CosmosLockService extends AbstractNoSqlLockService<CosmosLiquibaseDatabase> {

    private final Logger log = Scope.getCurrentScope().getLog(getClass());

    @Override
    public boolean supports(final Database database) {
        return CosmosLiquibaseDatabase.COSMOSDB_PRODUCT_NAME.equals(database.getDatabaseProductName());
    }

    @Override
    protected Boolean isLocked() throws DatabaseException {
        final Optional<CosmosChangeLogLock> lock = Optional.ofNullable(getExecutor()
                .queryForObject(new SelectChangeLogLockStatement(getDatabaseChangeLogLockTableName()), CosmosChangeLogLock.class));
        return lock.map(CosmosChangeLogLock::getLocked).orElse(FALSE);
    }

    @Override
    protected int replaceLock(final boolean locked) throws DatabaseException {
        return getExecutor().update(
                new ReplaceLockChangeLogStatement(getDatabaseChangeLogLockTableName(), locked)
        );
    }

    @Override
    protected List<DatabaseChangeLogLock> queryLocks() throws DatabaseException {

        final List<Object> rows =
                getExecutor().queryForList(new SelectChangeLogLocksStatement(getDatabaseChangeLogLockTableName()), CosmosChangeLogLock.class);
        return rows.stream().map(DatabaseChangeLogLock.class::cast).collect(Collectors.toList());
    }

    @Override
    protected Boolean existsRepository() throws DatabaseException {
        return getExecutor().queryForLong(new CountContainersByNameStatement(getDatabase().getDatabaseChangeLogLockTableName())) == 1L;
    }

    @Override
    protected void createRepository() throws DatabaseException {
        final CreateChangeLogLockContainerStatement createChangeLogLockContainerStatement =
                new CreateChangeLogLockContainerStatement(getDatabaseChangeLogLockTableName());
        getExecutor().execute(createChangeLogLockContainerStatement);
    }

    @Override
    protected void adjustRepository() throws DatabaseException {
        //NOOP
    }

    @Override
    protected void dropRepository() throws DatabaseException {
        getExecutor().execute(
                new DeleteContainerStatement(getDatabaseChangeLogLockTableName()));
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

}
