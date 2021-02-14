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

import liquibase.changelog.ChangeSet;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.List;

import static liquibase.ext.cosmosdb.TestUtils.getChangeSets;
import static org.assertj.core.api.Assertions.assertThat;

class UpsertItemChangeTest extends AbstractCosmosChangeTest {

    @Test
    void getConfirmationMessage() {
        assertThat(new UpsertItemChange().getConfirmationMessage()).isNotNull();
    }

    @Test
    @SneakyThrows
    void generateStatements() {
        final List<ChangeSet> changeSets = getChangeSets("liquibase/ext/change.upsert-item.test.xml", database);

        assertThat(changeSets).hasSize(3);
        assertThat(changeSets.get(0).getChanges())
            .hasSize(1)
            .hasOnlyElementsOfType(UpsertItemChange.class);
        assertThat(changeSets.get(1).getChanges())
            .hasSize(2)
            .hasOnlyElementsOfType(UpsertItemChange.class);
        assertThat(changeSets.get(2).getChanges())
            .hasSize(1)
            .hasOnlyElementsOfType(UpsertItemChange.class);

        assertThat(changeSets.get(0).getChanges().get(0))
            .hasFieldOrPropertyWithValue("containerId", "container1")
            .hasFieldOrPropertyWithValue("document", "{\"id\" : 1}");

        assertThat(changeSets.get(1).getChanges().get(0))
            .hasFieldOrPropertyWithValue("containerId", "container2")
            .hasFieldOrPropertyWithValue("document", "{\"id\" : 1}");

        assertThat(changeSets.get(1).getChanges().get(1))
            .hasFieldOrPropertyWithValue("containerId", "container3")
            .hasFieldOrPropertyWithValue("document", "{\"id\" : 1}");

        assertThat(changeSets.get(2).getChanges().get(0))
            .hasFieldOrPropertyWithValue("containerId", "container4")
            .hasFieldOrPropertyWithValue("document", null);
    }
}
