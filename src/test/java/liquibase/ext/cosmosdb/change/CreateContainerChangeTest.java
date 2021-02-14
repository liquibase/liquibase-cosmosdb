package liquibase.ext.cosmosdb.change;

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

import liquibase.change.Change;
import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;
import liquibase.database.core.PostgresDatabase;
import liquibase.ext.cosmosdb.database.CosmosLiquibaseDatabase;
import liquibase.ext.cosmosdb.statement.CreateContainerStatement;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static liquibase.ext.cosmosdb.TestUtils.getChangeSets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

class CreateContainerChangeTest extends AbstractCosmosChangeTest {

    @Test
    void getConfirmationMessage() {
        assertThat(new CreateContainerChange().getConfirmationMessage())
                .isNotNull();
    }

    @Test
    void supports() {
        assertThat(new CreateContainerChange().supports(new CosmosLiquibaseDatabase())).isTrue();
        assertThat(new CreateContainerChange().supports(new PostgresDatabase())).isFalse();
    }

    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    void testGenerateStatements() {

        final List<ChangeSet> changeSets = getChangeSets("liquibase/ext/changelog.create-container.test.xml", database);

        assertThat(changeSets)
                .isNotNull()
                .hasSize(2);

        assertThat(changeSets.get(0).generateCheckSum()).isEqualTo(CheckSum.parse("8:868c41109ad4c6dd47707e31a3cab8c8"));
        assertThat(changeSets.get(0).getChanges())
                .hasSize(5)
                .hasOnlyElementsOfType(CreateContainerChange.class)
                .extracting(c -> (CreateContainerChange) c)
                .extracting(
                        CreateContainerChange::getContainerName, CreateContainerChange::getSkipExisting, CreateContainerChange::getOptions, CreateContainerChange::getThroughput,
                        Change::generateCheckSum, c -> c.generateStatements(database).length, c -> c.generateStatements(database)[0].getClass()
                )
                .containsExactly(
                        tuple("minimal", null, null, null, CheckSum.parse("8:327fed49ce36964794facea53c0347d7"), 1, CreateContainerStatement.class),
                        tuple("minimal", TRUE, null, null, CheckSum.parse("8:eca3cd04f26a6b48945dfb2babf5ceda"), 1, CreateContainerStatement.class),
                        tuple("skipExisting", TRUE, null, null, CheckSum.parse("8:b3c7d43df39817432d284463344685d3"), 1, CreateContainerStatement.class),
                        tuple("skipExisting", TRUE, null, null, CheckSum.parse("8:b3c7d43df39817432d284463344685d3"), 1, CreateContainerStatement.class),
                        tuple("notSkipExisting", FALSE, null, null, CheckSum.parse("8:9d3d714841f4ebe33d6568a1d246efe9"), 1, CreateContainerStatement.class)
                );

        assertThat(changeSets.get(1).generateCheckSum()).isEqualTo(CheckSum.parse("8:d5eba9218b91327627c7174dd6630307"));
        assertThat(changeSets.get(1).getChanges())
                .hasSize(2)
                .hasOnlyElementsOfType(CreateContainerChange.class)
                .extracting(c -> (CreateContainerChange) c)
                .extracting(
                        CreateContainerChange::getContainerName, CreateContainerChange::getSkipExisting, c -> c.getOptions().length(), CreateContainerChange::getThroughput,
                        Change::generateCheckSum, c -> c.generateStatements(database).length, c -> c.generateStatements(database)[0].getClass()
                )
                .containsExactly(
                        tuple("maximal", null, 1548, "500", CheckSum.parse("8:96ff35442800b98c8054b4c0b19a6817"), 1, CreateContainerStatement.class),
                        tuple("maximalAutoRU", null, 790, "{\"maxThroughput\": 8000}", CheckSum.parse("8:ee1566c77e15d830c5b90edf1576067a"), 1, CreateContainerStatement.class)
                );

    }
}