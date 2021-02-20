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

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static liquibase.ext.cosmosdb.statement.JsonUtils.mergeDocuments;
import static liquibase.ext.cosmosdb.statement.JsonUtils.orEmptyDocument;
import static liquibase.ext.cosmosdb.statement.JsonUtils.toThroughputProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class JsonUtilsTest {

    @Test
    void testOrEmptyDocument() {
        assertThat(orEmptyDocument("{\"code\": \"fe4f70d0-d08d-86d0-6147-8d279a4fde9d\"}").getString("code"))
                .isEqualTo("fe4f70d0-d08d-86d0-6147-8d279a4fde9d");
        assertThat(orEmptyDocument(null).toJson()).isEqualTo("{}");
        assertThat(orEmptyDocument("").toJson()).isEqualTo("{}");
        assertThat(orEmptyDocument("{\"id\":1}").toJson()).isEqualTo("{\"id\":1}");
    }

    @Test
    void testOrEmptyDocumentSpecialChars() {
        assertThat(orEmptyDocument("{\"name\": \"Bank Złoto\"}").getString("name")).isEqualTo("Bank Złoto");
    }

    @Test
    void testOrEmptySqlQuerySpec() {

        String json = "{  \n" +
                "  \"query\": \"SELECT * FROM Families f WHERE f.id = @id AND f.Address.City = @city\",  \n" +
                "  \"parameters\": [  \n" +
                "    {  \n" +
                "      \"name\": \"@id\",  \n" +
                "      \"value\": \"AndersenFamily\"  \n" +
                "    },  \n" +
                "    {  \n" +
                "      \"name\": \"@city\",  \n" +
                "      \"value\": \"Seattle\"  \n" +
                "    }  \n" +
                "  ]  \n" +
                "}  ";

        SqlQuerySpec actual = JsonUtils.orEmptySqlQuerySpec(json);
        assertThat(actual.getQueryText()).isEqualTo("SELECT * FROM Families f WHERE f.id = @id AND f.Address.City = @city");
        assertThat(actual.getParameters())
                .isNotEmpty()
                .hasSize(2);
        assertThat(actual.getParameters().get(0).getName()).isEqualTo("@id");
        assertThat(actual.getParameters().get(0).getValue(String.class)).isEqualTo("AndersenFamily");
        assertThat(actual.getParameters().get(1).getName()).isEqualTo("@city");
        assertThat(actual.getParameters().get(1).getValue(String.class)).isEqualTo("Seattle");

        // Empty String
        actual = JsonUtils.orEmptySqlQuerySpec("");
        assertThat(actual.getQueryText()).isNull();
        assertThat(actual.getParameters()).isNotNull().isEmpty();

        // null
        actual = JsonUtils.orEmptySqlQuerySpec(null);
        assertThat(actual.getQueryText()).isNull();
        assertThat(actual.getParameters()).isNotNull().isEmpty();

        // Malformed
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> JsonUtils.orEmptySqlQuerySpec("{Malformed}"));

        // Extra fields
        json = "{  \n" +
                "  \"extraQuery\": \"SELECT * FROM Families f WHERE f.id = @id AND f.Address.City = @city\",  \n" +
                "  \"extraParameters\": [  \n" +
                "    {  \n" +
                "      \"name\": \"@id\",  \n" +
                "      \"value\": \"AndersenFamily\"  \n" +
                "    },  \n" +
                "    {  \n" +
                "      \"name\": \"@city\",  \n" +
                "      \"value\": \"Seattle\"  \n" +
                "    }  \n" +
                "  ]  \n" +
                "}  ";
        actual = JsonUtils.orEmptySqlQuerySpec(json);
        assertThat(actual.getQueryText()).isNull();
        assertThat(actual.getParameters()).isNotNull().isEmpty();
    }

    @Test
    void testFromMap() {
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMergeDocuments() {

        final Document destination = orEmptyDocument("{  \n" +
                "  \"id\": \"AndersenFamily\",  \n" +
                "  \"LastName\": \"Andersen\",  \n" +
                "  \"Parents\": [  \n" +
                "    {  \n" +
                "      \"FamilyName\": null,  \n" +
                "      \"FirstName\": \"Thomas\"  \n" +
                "    },  \n" +
                "    {  \n" +
                "      \"FamilyName\": null,  \n" +
                "      \"FirstName\": \"Mary Kay\"  \n" +
                "    }  \n" +
                "  ],  \n" +
                "  \"Children\": [  \n" +
                "    {  \n" +
                "      \"FamilyName\": null,  \n" +
                "      \"FirstName\": \"Henriette Thaulow\",  \n" +
                "      \"Gender\": \"female\",  \n" +
                "      \"Grade\": 5,  \n" +
                "      \"Pets\": [  \n" +
                "        {  \n" +
                "          \"GivenName\": \"Fluffy\"  \n" +
                "        }  \n" +
                "      ]  \n" +
                "    }  \n" +
                "  ],  \n" +
                "  \"Address\": {  \n" +
                "    \"State\": \"WA\",  \n" +
                "    \"County\": \"King\",  \n" +
                "    \"City\": \"Seattle\"  \n" +
                "  },  \n" +
                "  \"DestinationOnly\": true  \n" +
                "}  ");

        assertThat(destination.getId()).isEqualTo("AndersenFamily");
        assertThat(destination.get("LastName")).isEqualTo("Andersen");
        assertThat(destination.getList("Parents", Map.class, false)).hasSize(2);
        assertThat(destination.getList("Children", Map.class, false)).hasSize(1);
        assertThat(destination.getObject("Address", Map.class))
                .hasFieldOrPropertyWithValue("State", "WA")
                .hasFieldOrPropertyWithValue("County", "King")
                .hasFieldOrPropertyWithValue("City", "Seattle");
        assertThat(destination.get("DestinationOnly")).isEqualTo(true);
        assertThat(destination.get("SourceOnly")).isNull();

        final Document source = orEmptyDocument("{  \n" +
                "  \"LastName\": \"AndersenFromSource\",  \n" +
                "  \"Parents\": [  \n" +
                "    {  \n" +
                "      \"FamilyName\": null,  \n" +
                "      \"FirstName\": \"Thomas\"  \n" +
                "    }  \n" +
                "  ],  \n" +
                "  \"Address\": {  \n" +
                "    \"State\": \"WAFromSource\",  \n" +
                "    \"County\": \"KingFromSource\",  \n" +
                "    \"City\": null  \n" +
                "  },  \n" +
                "  \"SourceOnly\": true  \n" +
                "}  ");

        mergeDocuments(destination, source);

        assertThat(destination.getId()).isEqualTo("AndersenFamily");
        assertThat(destination.get("LastName")).isEqualTo("AndersenFromSource");
        assertThat(destination.getList("Parents", Map.class, false)).hasSize(1);
        assertThat(destination.getList("Children", Map.class, false)).hasSize(1);
        assertThat((Map<String, Object>)destination.getObject("Address", Map.class))
                .hasFieldOrPropertyWithValue("State", "WAFromSource")
                .hasFieldOrPropertyWithValue("County", "KingFromSource")
                .containsKey("City")
                .hasFieldOrPropertyWithValue("City", null);
        assertThat(destination.get("DestinationOnly")).isEqualTo(true);
        assertThat(destination.get("SourceOnly")).isEqualTo(true);

        // Ensure source remain the same in order to be reused
        assertThat(source.getId()).isNull();
        assertThat(source.get("LastName")).isEqualTo("AndersenFromSource");
        assertThat(source.getList("Parents", Map.class, false)).hasSize(1);
        assertThat(source.getList("Children", Map.class, false)).isNull();
        assertThat((Map<String, Object>) source.getObject("Address", Map.class))
                .hasFieldOrPropertyWithValue("State", "WAFromSource")
                .hasFieldOrPropertyWithValue("County", "KingFromSource")
                .containsKey("City")
                .hasFieldOrPropertyWithValue("City", null);
        assertThat(source.get("DestinationOnly")).isNull();
        assertThat(source.get("SourceOnly")).isEqualTo(true);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testToThroughputProperties() {
        assertThat(toThroughputProperties(null)).isNull();
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> toThroughputProperties("{500}"))
                .withMessage("Unable to parse JSON {500}");

        assertThat(toThroughputProperties("500")).isNotNull()
                .returns(500, ThroughputProperties::getManualThroughput)
                .returns(0, ThroughputProperties::getAutoscaleMaxThroughput);

        assertThat(toThroughputProperties(" {\"maxThroughput\": 800}")).isNotNull()
                .returns(800, ThroughputProperties::getAutoscaleMaxThroughput);

        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> toThroughputProperties(" {\"maxThroughput\": 800}").getManualThroughput());
    }
}
