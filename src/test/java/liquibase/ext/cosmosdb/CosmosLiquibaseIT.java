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
import liquibase.change.CheckSum;
import liquibase.ext.cosmosdb.changelog.CosmosRanChangeSet;
import liquibase.ext.cosmosdb.changelog.SelectChangeLogRanChangeSetsStatement;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static liquibase.ext.cosmosdb.statement.JsonUtils.QUERY_SELECT_ALL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class CosmosLiquibaseIT extends AbstractCosmosWithConnectionIntegrationTest {

    protected SelectChangeLogRanChangeSetsStatement findAllRanChangeSets;


    @BeforeEach
    protected void setUpEach() {
        super.setUpEach();
        findAllRanChangeSets = new SelectChangeLogRanChangeSetsStatement(database.getDatabaseChangeLogTableName());
    }

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
        assertThat(maximalProperties.getIndexingPolicy().getExcludedPaths()).isNotNull();

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

        assertThat(container.getScripts().readAllStoredProcedures().stream().map(CosmosStoredProcedureProperties::getId).anyMatch(p -> p.equals("sproc_3")))
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

        List<CosmosRanChangeSet> changeSets = findAllRanChangeSets.queryForList(database);
        assertThat(changeSets).hasSize(6)
                .extracting(CosmosRanChangeSet::getId, CosmosRanChangeSet::getOrderExecuted, CosmosRanChangeSet::getLastCheckSum)
                .containsExactly(
                        tuple("1", 1, CheckSum.parse("9:3f13f9648dae9becf2490b37df903b39")),
                        tuple("2", 2, CheckSum.parse("9:e51c5a0ad0d966a104afd10fa6a50828")),
                        tuple("3", 3, CheckSum.parse("9:338b3446e7e81bbdc875196e576b4bbe")),
                        tuple("4", 4, CheckSum.parse("9:d657420b15a4393a1f2eae527a36eb44")),
                        tuple("5", 5, CheckSum.parse("9:aae0c2c2896115a3893e25220263f20d")),
                        tuple("6", 6, CheckSum.parse("9:4533c1bd519366972974e667fd5eb51b")));

        List<Map<?, ?>> documents = cosmosDatabase.getContainer("container1").queryItems(QUERY_SELECT_ALL, null, Map.class)
                .stream().map(d -> (Map<?, ?>) d).collect(Collectors.toList());

        assertThat(documents).hasSize(1)
                .extracting(
                        d -> d.get("id"),
                        d -> d.get("oldField"),
                        d -> d.get("changeValueField"),
                        d -> d.get("sameValueField"),
                        d -> d.get("newField"),
                        d -> d.get("partition"))
                .containsExactly(
                        tuple("1", null, "New Value", "Remains Same", "Will be Added", "default")
                );

        documents = cosmosDatabase.getContainer("container2").queryItems(QUERY_SELECT_ALL, null, Map.class)
                .stream().map(d -> (Map<?, ?>) d).collect(Collectors.toList());

        assertThat(documents).hasSize(2)
                .extracting(
                        d -> d.get("id"),
                        d -> d.get("oldField"),
                        d -> d.get("changeValueField"),
                        d -> d.get("sameValueField"),
                        d -> d.get("newField"),
                        d -> d.get("partition"))
                .containsExactly(
                        tuple("1", "Will Remain", "Old Value", "Remains Same", null, "default1"),
                        tuple("1", null, "New Value", "Remains Same", "Will be Added", "default2")
                );
    }

    @SneakyThrows
    @Test
    void testUpdateUpdateEachItem() {
        final Liquibase liquibase = new Liquibase("liquibase/ext/changelog.update-each-item.test.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update("");

        List<CosmosRanChangeSet> changeSets = findAllRanChangeSets.queryForList(database);
        assertThat(changeSets).hasSize(8)
                .extracting(CosmosRanChangeSet::getId, CosmosRanChangeSet::getAuthor, CosmosRanChangeSet::getOrderExecuted, CosmosRanChangeSet::getLastCheckSum)
                .containsExactly(
                        tuple("1", "victor", 1, CheckSum.parse("9:ce185bf80e2edb000cf81b48fc8745ba")),
                        tuple("2", "victor", 2, CheckSum.parse("9:d4583e47d9f6cb59df1bddbe3886d775")),
                        tuple("3", "victor", 3, CheckSum.parse("9:6bc4779744be4301ff285b2649d383ac")),
                        tuple("4", "victor", 4, CheckSum.parse("9:eb1d4a019d93fd8a0896ff0c4d5443b1")),
                        tuple("1", "alex", 5, CheckSum.parse("9:82f70fb78ee2ef29b0a0bc6f3f7ed848")),
                        tuple("2", "alex", 6, CheckSum.parse("9:918ce4263ad25c49377b026c56fb143d")),
                        tuple("3", "alex", 7, CheckSum.parse("9:e8611284413844952b30986300d69e77")),
                        tuple("4", "alex", 8, CheckSum.parse("9:55f00216ff3592eea12a46fe5cb570d0")));

        // check non partitioned items
        List<Map<?, ?>> documents = cosmosDatabase.getContainer("container0").queryItems(QUERY_SELECT_ALL, null, Map.class)
                .stream().map(d -> (Map<?, ?>) d).collect(Collectors.toList());

        assertThat(documents).hasSize(3)
                .extracting(
                        d -> d.get("id"),
                        d -> d.get("changedField"),
                        d -> d.get("remainedField"),
                        d -> d.get("onlyIn1Field"),
                        d -> d.get("onlyIn2Field"),
                        d -> d.get("onlyIn3Field"),
                        d -> d.get("partition"))
                .containsExactly(
                        tuple("1", "Value Changed", "Remains Same1", "Remains Only1", null, null, "default"),
                        tuple("2", "Value Changed", "Remains Same2", null, "Remains Only2", null, "default"),
                        tuple("3", "Value Unchanged", "Remains Same3", null, null, "Remains Only3", "default")
                );

        // check partitioned items
        documents = cosmosDatabase.getContainer("container1").queryItems(QUERY_SELECT_ALL, null, Map.class)
                .stream().map(d -> (Map<?, ?>) d).collect(Collectors.toList());

        assertThat(documents).hasSize(3)
                .extracting(
                        d -> d.get("id"),
                        d -> d.get("changedField"),
                        d -> d.get("remainedField"),
                        d -> d.get("onlyIn1Field"),
                        d -> d.get("onlyIn2Field"),
                        d -> d.get("onlyIn3Field"),
                        d -> ((Map<?, ?>) d.get("partition")).get("field"))
                .containsExactly(
                        tuple("1", "Value Changed", "Remains Same1", "Remains Only1", null, null, "default1"),
                        tuple("2", "Value Changed", "Remains Same2", null, "Remains Only2", null, "default2"),
                        tuple("3", "Value Unchanged", "Remains Same3", null, null, "Remains Only3", "default3")
                );

    }

    @SneakyThrows
    @Test
    void testUpdateDeleteEachItem() {
        final Liquibase liquibase = new Liquibase("liquibase/ext/changelog.delete-each-item.test.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update("");

        List<CosmosRanChangeSet> changeSets = findAllRanChangeSets.queryForList(database);
        assertThat(changeSets).hasSize(6)
                .extracting(CosmosRanChangeSet::getId, CosmosRanChangeSet::getAuthor, CosmosRanChangeSet::getOrderExecuted, CosmosRanChangeSet::getLastCheckSum)
                .containsExactly(
                        tuple("1", "alex", 1, CheckSum.parse("9:e3668559782d993266bacd2da14e1dd1")),
                        tuple("2", "alex", 2, CheckSum.parse("9:99a1b76747adfe65be949de28b4a005a")),
                        tuple("3", "alex", 3, CheckSum.parse("9:bba0fcbc345f5bc46b703de37d2c014b")),
                        tuple("1", "victor", 4, CheckSum.parse("9:83f1ecd6d2e61c7b245123abf4367a2b")),
                        tuple("2", "victor", 5, CheckSum.parse("9:abbe0aca90e6b5bdfe23f025bc8a1082")),
                        tuple("3", "victor", 6, CheckSum.parse("9:52d4833bbabd302d39d44bf3a6dd1aa6"))
                );

        // check non partitioned items
        List<Map<?, ?>> documents = cosmosDatabase.getContainer("deleteEachContainer1").queryItems(QUERY_SELECT_ALL, null, Map.class)
                .stream().map(d -> (Map<?, ?>) d).collect(Collectors.toList());

        assertThat(documents).hasSize(1)
                .extracting(
                        d -> d.get("id"),
                        d -> d.get("name"))
                .containsExactly(
                        tuple("2", "Not To Be Deleted")
                );

        // check partitioned items
        documents = cosmosDatabase.getContainer("deleteEachContainer2").queryItems(QUERY_SELECT_ALL, null, Map.class)
                .stream().map(d -> (Map<?, ?>) d).collect(Collectors.toList());

        assertThat(documents).hasSize(1)
                .extracting(
                        d -> d.get("id"),
                        d -> d.get("name"),
                        d -> d.get("partition"))
                .containsExactly(
                        tuple("2", "Not To Be Deleted", "default2")
                );
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
