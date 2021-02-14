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

        assertThat(changeSets.get(0).generateCheckSum()).isEqualTo(CheckSum.parse("8:f9a902bc0f602d3c8f0ce07ebd8bb90b"));
        assertThat(changeSets.get(0).getChanges())
                .hasSize(5)
                .hasOnlyElementsOfType(CreateContainerChange.class)
                .extracting(c -> (CreateContainerChange) c)
                .extracting(
                        CreateContainerChange::getContainerId, CreateContainerChange::getSkipExisting, CreateContainerChange::getContainerProperties, CreateContainerChange::getThroughputProperties,
                        Change::generateCheckSum, c -> c.generateStatements(database).length, c -> c.generateStatements(database)[0].getClass()
                )
                .containsExactly(
                        tuple("minimal", null, null, null, CheckSum.parse("8:093e2225953ad4686ca0a89eeabc47ba"), 1, CreateContainerStatement.class),
                        tuple("minimal", TRUE, null, null, CheckSum.parse("8:adad58b87e2ef854fd678f0b03565dfe"), 1, CreateContainerStatement.class),
                        tuple("skipExisting", TRUE, null, null, CheckSum.parse("8:614e03910b2faf3e6983c894738a726a"), 1, CreateContainerStatement.class),
                        tuple("skipExisting", TRUE, null, null, CheckSum.parse("8:614e03910b2faf3e6983c894738a726a"), 1, CreateContainerStatement.class),
                        tuple("notSkipExisting", FALSE, null, null, CheckSum.parse("8:4b491837aa994bfab8801672021d47f6"), 1, CreateContainerStatement.class)
                );

        assertThat(changeSets.get(1).generateCheckSum()).isEqualTo(CheckSum.parse("8:be5d8bb6fd7531a948444b922ddedad2"));
        assertThat(changeSets.get(1).getChanges())
                .hasSize(2)
                .hasOnlyElementsOfType(CreateContainerChange.class)
                .extracting(c -> (CreateContainerChange) c)
                .extracting(
                        CreateContainerChange::getContainerId, CreateContainerChange::getSkipExisting, c -> c.getContainerProperties().length(), CreateContainerChange::getThroughputProperties,
                        Change::generateCheckSum, c -> c.generateStatements(database).length, c -> c.generateStatements(database)[0].getClass()
                )
                .containsExactly(
                        tuple("maximal", null, 1546, "500", CheckSum.parse("8:40b47a837cf71fa82bf2da026678cf16"), 1, CreateContainerStatement.class),
                        tuple("maximalAutoRU", null, 790, "{\"maxThroughput\": 8000}", CheckSum.parse("8:cf350b32c69fd4fed73991f3b3f10ffd"), 1, CreateContainerStatement.class)
                );

    }
}