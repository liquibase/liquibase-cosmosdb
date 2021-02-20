package liquibase.ext.cosmosdb;

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
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.ThroughputProperties;
import liquibase.Liquibase;
import liquibase.ext.cosmosdb.changelog.CosmosRanChangeSet;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static liquibase.ext.cosmosdb.statement.JsonUtils.QUERY_SELECT_ALL;
import static org.assertj.core.api.Assertions.assertThat;

class CosmosLiquibaseIT extends AbstractCosmosWithConnectionIntegrationTest {

    @SneakyThrows
    @Test
    @SuppressWarnings("unchecked")
    void testUpdate() {
        final Liquibase liquibase = new Liquibase("liquibase/ext/changelog.generic.test.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update("");
        assertThat(cosmosDatabase.getContainer(database.getDatabaseChangeLogLockTableName()).read()).isNotNull();
        assertThat(cosmosDatabase.getContainer(database.getDatabaseChangeLogTableName()).read()).isNotNull();
        assertThat(cosmosDatabase.readAllContainers().stream().count()).isEqualTo(3);

        final List<Map<String, Object>> ranChangeSets = cosmosDatabase.getContainer(database.getDatabaseChangeLogTableName()).queryItems(QUERY_SELECT_ALL, null, Map.class)
                .stream().map(i -> (Map<String, Object>) i).collect(Collectors.toList());
        assertThat(ranChangeSets).hasSize(2);
        assertThat(ranChangeSets.get(0))
                .hasFieldOrPropertyWithValue(CosmosRanChangeSet.Fields.ORDER_EXECUTED, 1);
        assertThat(ranChangeSets.get(1))
                .hasFieldOrPropertyWithValue(CosmosRanChangeSet.Fields.ORDER_EXECUTED, 2);
    }

    @SneakyThrows
    @Test
    void testUpdateCreateContainer() {
        final Liquibase liquibase = new Liquibase("liquibase/ext/changelog.create-container.test.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update("");
        assertThat(cosmosDatabase.getContainer(database.getDatabaseChangeLogLockTableName()).read()).isNotNull();
        assertThat(cosmosDatabase.getContainer(database.getDatabaseChangeLogTableName()).read()).isNotNull();

        List<CosmosContainerProperties> containerProperties = cosmosDatabase.readAllContainers().stream().collect(Collectors.toList());

        final CosmosContainerProperties minimal = containerProperties.stream().filter(c -> c.getId().equals("minimal")).findFirst().orElse(null);
        assertThat(minimal).isNotNull();

        final CosmosContainerProperties skipExisting = containerProperties.stream().filter(c -> c.getId().equals("skipExisting")).findFirst().orElse(null);
        assertThat(skipExisting).isNotNull();

        final CosmosContainerProperties maximal = containerProperties.stream().filter(c -> c.getId().equals("maximal")).findFirst().orElse(null);
        assertThat(maximal).isNotNull();

        assertThat(containerProperties).hasSize(7);
    }

    @SneakyThrows
    @Test
    void testUpdateReplaceContainer() {
        final Liquibase liquibase = new Liquibase("liquibase/ext/changelog.replace-container.test.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update("");
        assertThat(cosmosDatabase.getContainer(database.getDatabaseChangeLogLockTableName()).read()).isNotNull();
        assertThat(cosmosDatabase.getContainer(database.getDatabaseChangeLogTableName()).read()).isNotNull();

        assertThat(cosmosDatabase.getContainer("minimal").read()).isNotNull();
        assertThat(cosmosDatabase.getContainer("minimal").read().getProperties()).isNotNull();
        assertThat(cosmosDatabase.getContainer("minimal").readThroughput()).isNotNull();

        final CosmosContainerProperties maximalProperties = cosmosDatabase.getContainer("maximal").read().getProperties();
        assertThat(maximalProperties).isNotNull();
        assertThat(maximalProperties.getId()).isEqualTo("maximal");
        //TODO: Review after fixed replace
        assertThat(maximalProperties.getIndexingPolicy().getIndexingMode()).isEqualTo(IndexingMode.CONSISTENT);
        assertThat(maximalProperties.getIndexingPolicy().isAutomatic()).isTrue();
        assertThat(maximalProperties.getIndexingPolicy().getIncludedPaths()).hasSize(1);
        assertThat(maximalProperties.getIndexingPolicy().getExcludedPaths()).hasSize(1);

        final ThroughputProperties maximalThroughput = cosmosDatabase.getContainer("maximal").readThroughput().getProperties();
        assertThat(maximalThroughput).isNotNull();
        assertThat(maximalThroughput.getManualThroughput()).isEqualTo(800);
    }

    @SneakyThrows
    @Test
    void testUpdateCreateProcedure() {
        final Liquibase liquibase = new Liquibase("liquibase/ext/changelog.create-procedure.test.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update("");
        assertThat(cosmosDatabase.getContainer(database.getDatabaseChangeLogLockTableName()).read()).isNotNull();
        assertThat(cosmosDatabase.getContainer(database.getDatabaseChangeLogTableName()).read()).isNotNull();

        final CosmosContainer container = cosmosDatabase.getContainer("procedureContainer1");
        assertThat(container).isNotNull();

        assertThat(container.getScripts().getStoredProcedure("sproc_1").read().getProperties())
                .isNotNull()
                .returns("sproc_1", CosmosStoredProcedureProperties::getId)
                .returns("function sproc_1() {var context = getContext(); var response = context.getResponse(); response.setBody(1);}",
                        CosmosStoredProcedureProperties::getBody);

        // TODO: Replace not working fails with IllegalArgumentException: Entity with the specified id does not exist in the system.

        //        assertThat(container.getScripts().getStoredProcedure("sproc_2").read().getProperties())
        //                .isNotNull()
        //                .returns("sproc_2", CosmosStoredProcedureProperties::getId)
        //                .returns("function() sproc_2 {var context = getContext(); var response = context.getResponse(); response.setBody(\"Replaced\");}",
        //                CosmosStoredProcedureProperties::getBody);

        assertThat(container.getScripts().readAllStoredProcedures().stream().map(CosmosStoredProcedureProperties::getId).anyMatch( p -> p.equals("sproc_3")))
                .isFalse();
    }

    @SneakyThrows
    @Test
    void testUpdateDeleteContainer() {

        assertThat(cosmosDatabase.readAllContainers().stream().count()).isEqualTo(0L);

        final Liquibase liquibase = new Liquibase("liquibase/ext/changelog.delete-container.test.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update("");
        assertThat(cosmosDatabase.getContainer(database.getDatabaseChangeLogLockTableName()).read()).isNotNull();
        assertThat(cosmosDatabase.getContainer(database.getDatabaseChangeLogTableName()).read()).isNotNull();
        assertThat(cosmosDatabase.getContainer(database.getDatabaseChangeLogTableName())
                .queryItems(QUERY_SELECT_ALL, null, Map.class)
                .stream().count()).isEqualTo(2L);

        assertThat(cosmosDatabase.readAllContainers().stream().map(CosmosContainerProperties::getId).collect(Collectors.toList()))
                .hasSize(3)
                .contains("container4")
                .doesNotContain("container1", "container2", "container3", "container5");
    }

    @SneakyThrows
    @Test
    void testUpdateIncremental() {

        // First increment
        Liquibase liquibase = new Liquibase("liquibase/ext/changelog.incremental-1.main.xml", new ClassLoaderResourceAccessor(), database);

        assertThat(cosmosDatabase.readAllContainers().stream()
                .filter(c -> c.getId().equals(database.getDatabaseChangeLogTableName())).findFirst())
                .isNotPresent();
        assertThat(cosmosDatabase.readAllContainers().stream()
                .filter(c -> c.getId().equals(database.getDatabaseChangeLogLockTableName())).findFirst())
                .isNotPresent();

        liquibase.update("");

        assertThat(cosmosDatabase.readAllContainers().stream()
                .filter(c -> c.getId().equals(database.getDatabaseChangeLogTableName())).findFirst())
                .isPresent();

        assertThat(cosmosDatabase.readAllContainers().stream()
                .filter(c -> c.getId().equals(database.getDatabaseChangeLogLockTableName())).findFirst())
                .isPresent();

        assertThat(cosmosDatabase.readAllContainers().stream()
                .filter(c -> c.getId().equals("container1")).findFirst())
                .isPresent();

        assertThat(cosmosDatabase.readAllContainers().stream()
                .filter(c -> c.getId().equals("container2")).findFirst())
                .isNotPresent();

        assertThat(cosmosDatabase.readAllContainers().stream().count()).isEqualTo(3);

        // Second increment
        liquibase = new Liquibase("liquibase/ext/changelog.incremental-2.main.xml", new ClassLoaderResourceAccessor(), database);

        assertThat(cosmosDatabase.readAllContainers().stream()
                .filter(c -> c.getId().equals(database.getDatabaseChangeLogTableName())).findFirst())
                .isPresent();
        assertThat(cosmosDatabase.readAllContainers().stream()
                .filter(c -> c.getId().equals(database.getDatabaseChangeLogLockTableName())).findFirst())
                .isPresent();

        liquibase.update("");

        assertThat(cosmosDatabase.readAllContainers().stream()
                .filter(c -> c.getId().equals(database.getDatabaseChangeLogTableName())).findFirst())
                .isPresent();

        assertThat(cosmosDatabase.readAllContainers().stream()
                .filter(c -> c.getId().equals(database.getDatabaseChangeLogLockTableName())).findFirst())
                .isPresent();

        assertThat(cosmosDatabase.readAllContainers().stream()
                .filter(c -> c.getId().equals("container1")).findFirst())
                .isPresent();

        assertThat(cosmosDatabase.readAllContainers().stream()
                .filter(c -> c.getId().equals("container2")).findFirst())
                .isPresent();

        assertThat(cosmosDatabase.readAllContainers().stream().count()).isEqualTo(4);


    }

    @SneakyThrows
    @Test
    void testUpdateCreateItem() {
        final Liquibase liquibase = new Liquibase("liquibase/ext/changelog.create-item.test.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update("");

        Map<?, ?> document = cosmosDatabase.getContainer("container1").queryItems(QUERY_SELECT_ALL, null, Map.class)
                .stream().filter(i -> ((Map<?, ?>) i).get("id").equals("1")).findFirst().orElse(null);

        assertThat(document).isNotNull();
        assertThat(document.get("specialField($)!")).isEqualTo("Special char in field name");
        assertThat(document.get("integerField")).isEqualTo(100);
        assertThat(document.get("numberField")).isEqualTo(-12.2);
        assertThat(document.get("nullField")).isNull();
        assertThat((ArrayList<?>) document.get("arrayField")).hasSize(3);
        assertThat((ArrayList<?>) document.get("emptyArray")).isEmpty();
        assertThat((Map<?, ?>) document.get("nestedObject")).hasSize(2);
        assertThat(((Map<?, ?>) document.get("nestedObject")).get("nestedField")).isEqualTo("nestedValue");
        assertThat((ArrayList<?>) ((Map<?, ?>) document.get("nestedObject")).get("nestedArrayField")).hasSize(3);
        assertThat((Map<?, ?>) document.get("nestedEmptyObject")).isEmpty();
        assertThat(document.get("partition")).isEqualTo("default");
    }

    @SneakyThrows
    @Test
    void testUpdateUpsertItem() {
        final Liquibase liquibase = new Liquibase("liquibase/ext/changelog.upsert-item.test.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update("");

        // createContainer, createItem, upsertItem = 3 rows in log
        assertThat(cosmosDatabase.getContainer(database.getDatabaseChangeLogTableName())
                .queryItems(QUERY_SELECT_ALL, null, Map.class).stream().count()).isEqualTo(3L);

        Map<?, ?> document = cosmosDatabase.getContainer("container1").queryItems(QUERY_SELECT_ALL, null, Map.class)
                .stream().filter(i -> ((Map<?, ?>) i).get("id").equals("1")).findFirst().orElse(null);

        assertThat(document).isNotNull();
        assertThat(document.get("oldField")).isNull();
        assertThat(document.get("changeValueField")).isEqualTo("New Value");
        assertThat(document.get("sameValueField")).isEqualTo("Remains Same");
        assertThat(document.get("newField")).isEqualTo("Will be Added");
        assertThat(document.get("partition")).isEqualTo("default");
    }


    @SneakyThrows
    @Test
    @SuppressWarnings("unchecked")
    void testUpdateUpdateEachItem() {
        final Liquibase liquibase = new Liquibase("liquibase/ext/changelog.update-each-item.test.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update("");

        // createContainer, createItem, upsertItem = 3 rows in log
        assertThat(cosmosDatabase.getContainer(database.getDatabaseChangeLogTableName())
                .queryItems(QUERY_SELECT_ALL, null, Map.class).stream().count()).isEqualTo(4L);

        Map<String, ?> document = cosmosDatabase.getContainer("container1").queryItems(QUERY_SELECT_ALL, null, Map.class)
                .stream().filter(i -> ((Map<String, ?>) i).get("id").equals("1")).findFirst().orElse(null);

        assertThat(document).isNotNull()
                .hasFieldOrPropertyWithValue("id", "1")
                .hasFieldOrPropertyWithValue("changedField", "Value Changed")
                .hasFieldOrPropertyWithValue("remainedField", "Remains Same1")
                .hasFieldOrPropertyWithValue("onlyIn1Field", "Remains Only1")
                .doesNotContainKey("onlyIn2Field")
                .doesNotContainKey("onlyIn3Field")
                .hasFieldOrPropertyWithValue("addedField", "Added Value")
                .hasFieldOrPropertyWithValue("partition", "default");

        document = cosmosDatabase.getContainer("container1").queryItems(QUERY_SELECT_ALL, null, Map.class)
                .stream().filter(i -> ((Map<String, ?>) i).get("id").equals("2")).findFirst().orElse(null);

        assertThat(document).isNotNull()
                .hasFieldOrPropertyWithValue("id", "2")
                .hasFieldOrPropertyWithValue("changedField", "Value Changed")
                .hasFieldOrPropertyWithValue("remainedField", "Remains Same2")
                .hasFieldOrPropertyWithValue("onlyIn2Field", "Remains Only2")
                .doesNotContainKey("onlyIn1Field")
                .doesNotContainKey("onlyIn3Field")
                .hasFieldOrPropertyWithValue("addedField", "Added Value")
                .hasFieldOrPropertyWithValue("partition", "default");

        document = cosmosDatabase.getContainer("container1").queryItems(QUERY_SELECT_ALL, null, Map.class)
                .stream().filter(i -> ((Map<String, ?>) i).get("id").equals("3")).findFirst().orElse(null);

        assertThat(document).isNotNull()
                .hasFieldOrPropertyWithValue("id", "3")
                .hasFieldOrPropertyWithValue("changedField", "Value Unchanged")
                .hasFieldOrPropertyWithValue("remainedField", "Remains Same3")
                .hasFieldOrPropertyWithValue("onlyIn3Field", "Remains Only3")
                .doesNotContainKey("onlyIn1Field")
                .doesNotContainKey("onlyIn2Field")
                .doesNotContainKey("addedField")
                .hasFieldOrPropertyWithValue("partition", "default");
    }

    @SneakyThrows
    @Test
    @SuppressWarnings("unchecked")
    void testUpdateDeleteEachItem() {
        final Liquibase liquibase = new Liquibase("liquibase/ext/changelog.delete-each-item.test.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update("");

        // createContainer, createItem, upsertItem = 3 rows in log
        assertThat(cosmosDatabase.getContainer(database.getDatabaseChangeLogTableName())
                .queryItems(QUERY_SELECT_ALL, null, Map.class).stream().count()).isEqualTo(3L);

        List<Map<String, ?>> documents = cosmosDatabase.getContainer("deleteEachContainer1").queryItems(QUERY_SELECT_ALL, null, Map.class)
                .stream().map(d->(Map<String, ?>)d).collect(Collectors.toList());

        assertThat(documents).hasSize(1).first()
                .returns("2", d->d.get("id"))
                .returns("Not To Be Deleted", d->d.get("name"));
    }

    @Test
    void testClearChecksums() {
        //Liquibase liquibase = new Liquibase("liquibase/ext/changelog.create-item.test.xml", new ClassLoaderResourceAccessor(), cosmosLiquibaseDatabase);
        //TODO: liquibase.clearCheckSums();
    }

    @Test
    void testClearScopes() {
        //Liquibase liquibase = new Liquibase("liquibase/ext/changelog.create-item.test.xml", new ClassLoaderResourceAccessor(), cosmosLiquibaseDatabase);
        //TODO: scopes;
    }

    @SneakyThrows
    @Test
    void testDropAll() {
        Liquibase liquibase = new Liquibase("liquibase/ext/changelog.generic.test.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.dropAll();
        assertThat(cosmosDatabase.readAllContainers().stream().count()).isEqualTo(0);

    }

}
