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

import com.azure.cosmos.CosmosScripts;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import liquibase.ext.cosmosdb.database.CosmosLiquibaseDatabase;
import liquibase.nosql.statement.NoSqlExecuteStatement;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static java.lang.Boolean.FALSE;
import static java.util.Optional.ofNullable;
import static liquibase.ext.cosmosdb.statement.JsonUtils.orEmptyStoredProcedureProperties;

@Getter
@EqualsAndHashCode(callSuper = true)
public class DeleteStoredProcedureStatement extends AbstractCosmosContainerStatement
        implements NoSqlExecuteStatement<CosmosLiquibaseDatabase> {

    public static final String COMMAND_NAME = "deleteStoredProcedure";

    private final CosmosStoredProcedureProperties procedureProperties;
    private final Boolean skipMissing;

    public DeleteStoredProcedureStatement(final String containerName, final String procedurePropertiesJson, final Boolean skipMissing) {
        this(containerName, orEmptyStoredProcedureProperties(procedurePropertiesJson), skipMissing);
    }

    public DeleteStoredProcedureStatement(final String containerName, final CosmosStoredProcedureProperties procedureProperties, final Boolean skipMissing) {
        super(containerName);
        this.procedureProperties = procedureProperties;
        this.skipMissing = ofNullable(skipMissing).orElse(FALSE);
    }

    public DeleteStoredProcedureStatement() {
        this(null, (String) null, null);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String toJs() {
        return
                "db." + getContainerName() + "."
                        + getCommandName()
                        + "("
                        + procedureProperties.toString()
                        + ");";
    }

    @Override
    public void execute(final CosmosLiquibaseDatabase database) {

        final CosmosScripts cosmosScripts = database.getCosmosDatabase().getContainer(getContainerName()).getScripts();

        if (skipMissing) {
            if (cosmosScripts.readAllStoredProcedures()
                    .stream().noneMatch(p -> p.getId().equals(procedureProperties.getId()))) {
                // Do nothing as #skipMissing is TRUE and procedure is not found
                return;
            }
        }
        cosmosScripts.getStoredProcedure(procedureProperties.getId()).delete();
    }

}
