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
public class CreateStoredProcedureStatement extends AbstractCosmosContainerStatement
        implements NoSqlExecuteStatement<CosmosLiquibaseDatabase> {

    public static final String COMMAND_NAME = "createStoredProcedure";

    private final CosmosStoredProcedureProperties procedureProperties;
    private final Boolean replaceExisting;

    public CreateStoredProcedureStatement() {
        this(null, (String) null, null);
    }

    public CreateStoredProcedureStatement(String containerName, String procedurePropertiesJson, Boolean replaceExisting) {
        this(containerName, orEmptyStoredProcedureProperties(procedurePropertiesJson), replaceExisting);
    }

    public CreateStoredProcedureStatement(String containerName, CosmosStoredProcedureProperties procedureProperties, Boolean replaceExisting) {
        super(containerName);
        this.procedureProperties = procedureProperties;
        this.replaceExisting = ofNullable(replaceExisting).orElse(FALSE);
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

        final CosmosScripts cosmosScripts = database.getCosmosDatabase()
                .getContainer(getContainerName()).getScripts();

        if (replaceExisting) {
            //TODO: Not working, not clear why
            cosmosScripts.getStoredProcedure(procedureProperties.getId())
                    .replace(procedureProperties);
        } else {
            cosmosScripts.createStoredProcedure(procedureProperties);
        }
    }

}
