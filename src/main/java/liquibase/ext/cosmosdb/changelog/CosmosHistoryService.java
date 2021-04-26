package liquibase.ext.cosmosdb.changelog;

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
import liquibase.change.Change;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.ext.cosmosdb.database.CosmosLiquibaseDatabase;
import liquibase.ext.cosmosdb.statement.CountContainersByNameStatement;
import liquibase.ext.cosmosdb.statement.CountDocumentsInContainerStatement;
import liquibase.ext.cosmosdb.statement.DeleteContainerStatement;
import liquibase.logging.Logger;
import liquibase.nosql.changelog.AbstractNoSqlHistoryService;
import liquibase.nosql.executor.NoSqlExecutor;
import liquibase.util.StringUtil;

import java.util.List;
import java.util.stream.Collectors;

import static liquibase.plugin.Plugin.PRIORITY_SPECIALIZED;

public class CosmosHistoryService extends AbstractNoSqlHistoryService<CosmosLiquibaseDatabase> {

    private final Logger log = Scope.getCurrentScope().getLog(getClass());

    @Override
    public int getPriority() {
        return PRIORITY_SPECIALIZED;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    public boolean supports(final Database database) {
        return CosmosLiquibaseDatabase.COSMOSDB_PRODUCT_NAME.equals(database.getDatabaseProductName());
    }

    @Override
    protected Boolean existsRepository() throws DatabaseException {
        return getExecutor().queryForLong(
                new CountContainersByNameStatement(this.getDatabaseChangeLogTableName())) == 1L;
    }

    @Override
    protected void createRepository() throws DatabaseException {
        final CreateChangeLogContainerStatement createChangeLogContainerStatement =
                new CreateChangeLogContainerStatement(this.getDatabaseChangeLogTableName());
        getExecutor().execute(createChangeLogContainerStatement);

    }

    @Override
    protected void adjustRepository() throws DatabaseException {
        //NOOP
    }

    @Override
    protected void dropRepository() throws DatabaseException {
        getExecutor().execute(
                new DeleteContainerStatement(getDatabaseChangeLogTableName()));
    }

    @Override
    protected List<RanChangeSet> queryRanChangeSets() throws DatabaseException {

        return getExecutor().queryForList(new SelectChangeLogRanChangeSetsStatement(getDatabaseChangeLogTableName()), RanChangeSet.class)
                .stream().map(RanChangeSet.class::cast).collect(Collectors.toList());
    }

    @Override
    protected Integer generateNextSequence() throws DatabaseException {
        return (int) getExecutor().queryForLong(new GetNextChangeSetSequenceValueStatement(getDatabaseChangeLogTableName()));
    }

    @Override
    protected void markChangeSetRun(final ChangeSet changeSet, final ChangeSet.ExecType execType, final Integer nextSequenceValue)
            throws DatabaseException {

        final NoSqlExecutor executor = getExecutor();

        final MarkChangeSetRanStatement markChangeSetRanStatement =
                new MarkChangeSetRanStatement(getDatabaseChangeLogTableName(), changeSet, execType, nextSequenceValue, getDeploymentId());

        executor.execute(markChangeSetRanStatement);

    }

    //TODO: Raise with Liquibase to make it as part of ChangeSet class
    public String extractTag(final ChangeSet changeSet) {
        String tag = null;
        for (Change change : changeSet.getChanges()) {
            if (change instanceof TagDatabaseChange) {
                TagDatabaseChange tagChange = (TagDatabaseChange) change;
                tag = StringUtil.trimToNull(tagChange.getTag());
            }
        }
        return tag;
    }

    @Override
    protected void removeRanChangeSet(final ChangeSet changeSet) throws DatabaseException {
        //TODO: Implement
    }

    @Override
    protected void clearChekSums() throws DatabaseException {
        //TODO: Implement
    }

    @Override
    protected long countTags(final String tag) throws DatabaseException {
        //TODO: Implement
        return 0;
    }

    @Override
    protected void tagLast(final String tagString) throws DatabaseException {
        //TODO: Implement
    }

    @Override
    protected long countRanChangeSets() throws DatabaseException {
        return getExecutor().queryForLong(new CountDocumentsInContainerStatement(getDatabaseChangeLogTableName()));
    }

    @Override
    protected void updateCheckSum(final ChangeSet changeSet) throws DatabaseException {
        //TODO: Implement
    }
}
