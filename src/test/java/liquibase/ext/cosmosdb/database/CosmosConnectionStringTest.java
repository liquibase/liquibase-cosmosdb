package liquibase.ext.cosmosdb.database;

import com.azure.cosmos.ConnectionMode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static liquibase.ext.cosmosdb.database.CosmosConnectionString.fromConnectionString;
import static liquibase.ext.cosmosdb.database.CosmosConnectionString.fromJsonConnectionString;
import static liquibase.ext.cosmosdb.database.CosmosConnectionString.fromUrlConnectionString;
import static liquibase.ext.cosmosdb.database.CosmosConnectionString.fromValues;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThat;

class CosmosConnectionStringTest {

    public static final String TEST_COSMOS_JSON_CONNECTION_STRING_1 =
            "cosmosdb://{\"accountEndpoint\" : \"https://ech-0a9d975b:8080\", \"accountKey\" : \"C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==\", \"databaseName\" : \"testdb1\" , \"ssl\" : \"true\" }";

    public static final String TEST_COSMOS_URL_CONNECTION_STRING_1 =
            "cosmosdb://ech-0a9d975b:C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==@ech-0a9d975b:8080/testdb1?ssl=false";



    @Test
    void fromJsonConnectionStringTest() {

        assertThatIllegalArgumentException().isThrownBy(
                () -> fromConnectionString(null)
        );

        assertThatIllegalArgumentException().isThrownBy(
                () -> fromConnectionString("")
        );

        assertThatIllegalArgumentException().isThrownBy(
                () -> fromConnectionString("mongodb://{wrong driver name}")
        );

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> fromConnectionString("cosmosdb://{not a json")
        );

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> fromConnectionString("cosmosdb://{wrong json payload}")
        );

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> fromJsonConnectionString("cosmosdb://{\"databaseName\" : \"db1\", \"databaseName\" : \"db2\"}")
        );
    }

    @Test
    void fromUrlConnectionStringTest() {

        assertThatIllegalArgumentException().isThrownBy(
                () -> fromConnectionString(null)
        );

        assertThatIllegalArgumentException().isThrownBy(
                () -> fromConnectionString("")
        );

        assertThatIllegalArgumentException().isThrownBy(
                () -> fromConnectionString("mongodb://wrong driver name")
        );

        assertThatExceptionOfType(Exception.class).isThrownBy(
                () -> fromUrlConnectionString("cosmosdb://\"databaseName\" : \"db1\", \"databaseName\" : \"db2\"")
        );
    }

    @Test
    void getJsonPropertyTest() {
        assertThat(fromJsonConnectionString("cosmosdb://{}").getProperty("any")).isNotPresent();
        assertThat(fromJsonConnectionString("cosmosdb://{\"aProperty\" : \"aValue\"}").getProperty("any")).isNotPresent();
        assertThat(fromJsonConnectionString("cosmosdb://{\"aProperty\" : \"aValue\"}").getProperty("aProperty")).hasValue("aValue");

        assertThat(fromJsonConnectionString("cosmosdb://{\"accountEndpoint\" : \"http://localhost:8080/\"}").getProperty(CosmosConnectionString.ACCOUNT_ENDPOINT_PROPERTY)).hasValue("http://localhost:8080/");
        assertThat(fromJsonConnectionString("cosmosdb://{\"accountEndpoint\" : \"http://localhost:8080/\"}").getAccountEndpoint()).hasValue("http://localhost:8080/");

        assertThat(fromJsonConnectionString("cosmosdb://{\"accountKey\" : \"key\"}").getProperty(CosmosConnectionString.ACCOUNT_KEY_PROPERTY)).hasValue("key");
        assertThat(fromJsonConnectionString("cosmosdb://{\"accountKey\" : \"key\"}").getAccountKey()).hasValue("key");

        assertThat(fromJsonConnectionString("cosmosdb://{\"databaseName\" : \"db1\"}").getProperty(CosmosConnectionString.DATABASE_NAME_PROPERTY)).hasValue("db1");
        assertThat(fromJsonConnectionString("cosmosdb://{\"databaseName\" : \"db1\"}").getDatabaseName()).hasValue("db1");

        assertThat(fromJsonConnectionString("cosmosdb://{\"connectionMode\" : \"gateway\"}").getProperty(CosmosConnectionString.CONNECTION_MODE_PROPERTY)).hasValue("gateway");
        assertThat(fromJsonConnectionString("cosmosdb://{\"connectionMode\" : \"gateway\"}").getConnectionMode()).hasValue(ConnectionMode.GATEWAY);
        assertThat(fromJsonConnectionString("cosmosdb://{}").getConnectionMode()).isNotPresent();

        final CosmosConnectionString cosmosConnectionString
                = fromJsonConnectionString("cosmosdb://{\"accountEndpoint\" : \"http://localhost:8080/\", \"accountKey\" : \"key\", \"databaseName\" : \"db1\"}");
        assertThat(cosmosConnectionString.getAccountEndpoint()).hasValue("http://localhost:8080/");
        assertThat(cosmosConnectionString.getAccountKey()).hasValue("key");
        assertThat(cosmosConnectionString.getDatabaseName()).hasValue("db1");
        assertThat(cosmosConnectionString.getProperty("any")).isNotPresent();
    }

    @Test
    void getUrlPropertyTest() {
        assertThat(fromUrlConnectionString("cosmosdb://user:key@host:8080/dbname").getProperty("any")).isNotPresent();
        assertThat(fromUrlConnectionString("cosmosdb://localhost:key@localhost:8080/db?aProperty=aValue").getProperty("any")).isNotPresent();
        assertThat(fromUrlConnectionString("cosmosdb://localhost:key@localhost:8080/db?aProperty=aValue").getProperty("aProperty")).hasValue("aValue");

        assertThat(fromUrlConnectionString("cosmosdb://localhost:key@localhost:8080/db?aProperty=aValue").getProperty(CosmosConnectionString.ACCOUNT_ENDPOINT_PROPERTY)).hasValue("https://localhost:8080");
        assertThat(fromUrlConnectionString("cosmosdb://localhost:key@localhost:8080/db?aProperty=aValue").getAccountEndpoint().orElse("")).isEqualTo("https://localhost:8080");

        assertThat(fromUrlConnectionString("cosmosdb://localhost:key@localhost:8080/db?aProperty=aValue&accountKey=key").getProperty(CosmosConnectionString.ACCOUNT_KEY_PROPERTY)).hasValue("key");
        assertThat(fromUrlConnectionString("cosmosdb://localhost:key@localhost:8080/db?accountKey=key").getAccountKey()).hasValue("key");

        assertThat(fromUrlConnectionString("cosmosdb://localhost:key@localhost:8080/db1?accountKey=key").getProperty(CosmosConnectionString.DATABASE_NAME_PROPERTY)).hasValue("db1");
        assertThat(fromUrlConnectionString("cosmosdb://localhost:key@localhost:8080/db1?accountKey=key").getDatabaseName()).hasValue("db1");

        assertThat(fromUrlConnectionString("cosmosdb://localhost:key@localhost:8080/db?connectionMode=gateway").getProperty(CosmosConnectionString.CONNECTION_MODE_PROPERTY)).hasValue("gateway");
        assertThat(fromUrlConnectionString("cosmosdb://localhost:key@localhost:8080/db?connectionMode=gateway").getConnectionMode()).hasValue(ConnectionMode.GATEWAY);
        assertThat(fromUrlConnectionString("cosmosdb://localhost:key@localhost:8080/db").getConnectionMode()).isNotPresent();

        final CosmosConnectionString cosmosConnectionString
                = fromUrlConnectionString("cosmosdb://localhost:key@localhost:8080/db1?accountKey=key");
        assertThat(cosmosConnectionString.getAccountEndpoint()).hasValue("https://localhost:8080");
        assertThat(cosmosConnectionString.getAccountKey()).hasValue("key");
        assertThat(cosmosConnectionString.getDatabaseName()).hasValue("db1");
        assertThat(cosmosConnectionString.getProperty("any")).isNotPresent();
    }

    @Test
    void fromValuesTest() {
        final CosmosConnectionString cosmosConnectionString
                = fromValues("http://localhost:8080/", "key", "db1");
        assertThat(cosmosConnectionString.getConnectionString()).isNull();
        assertThat(cosmosConnectionString.getAccountEndpoint()).hasValue("http://localhost:8080/");
        assertThat(cosmosConnectionString.getAccountKey()).hasValue("key");
        assertThat(cosmosConnectionString.getDatabaseName()).hasValue("db1");
        assertThat(cosmosConnectionString.getProperty("any")).isNotPresent();
        assertThat(cosmosConnectionString.toUrl()).isEqualTo("cosmosdb://{\"accountEndpoint\":\"http://localhost:8080/\",\"databaseName\":\"db1\",\"accountKey\":\"*****\"}");
    }

    @Nested
    class when_getConnectionMode_is_invoked {
        @Test
        void given_no_connectionMode_then_empty_is_returned() {
            assertThat(fromJsonConnectionString("cosmosdb://{}").getConnectionMode()).isNotPresent();
        }

        @Test
        void given_gateway_in_any_case_then_GATEWAY_is_returned() {
            assertThat(fromJsonConnectionString("cosmosdb://{\"connectionMode\" : \"gateway\"}").getConnectionMode()).hasValue(ConnectionMode.GATEWAY);
            assertThat(fromJsonConnectionString("cosmosdb://{\"connectionMode\" : \"GATEWAY\"}").getConnectionMode()).hasValue(ConnectionMode.GATEWAY);
            assertThat(fromJsonConnectionString("cosmosdb://{\"connectionMode\" : \"Gateway\"}").getConnectionMode()).hasValue(ConnectionMode.GATEWAY);
        }

        @Test
        void given_direct_then_DIRECT_is_returned() {
            assertThat(fromJsonConnectionString("cosmosdb://{\"connectionMode\" : \"direct\"}").getConnectionMode()).hasValue(ConnectionMode.DIRECT);
        }

        @Test
        void given_an_invalid_value_then_an_IllegalArgumentException_is_thrown() {
            assertThatIllegalArgumentException().isThrownBy(
                    () -> fromJsonConnectionString("cosmosdb://{\"connectionMode\" : \"bogus\"}").getConnectionMode());
        }
    }

    @Test
    void fromConnectionStringTest() {
        final CosmosConnectionString cosmosJsonConnectionString
                = fromConnectionString(TEST_COSMOS_JSON_CONNECTION_STRING_1);

        assertThat(cosmosJsonConnectionString)
                .returns(Optional.of("https://ech-0a9d975b:8080"), CosmosConnectionString::getAccountEndpoint)
                .returns(Optional.of("C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw=="), CosmosConnectionString::getAccountKey)
                .returns(Optional.of("testdb1"), CosmosConnectionString::getDatabaseName)
                .returns(Optional.of("true"), c -> c.getProperty("ssl"))
                .returns(Optional.empty(), c -> c.getProperty("notExisting"))
                .returns(TEST_COSMOS_JSON_CONNECTION_STRING_1, CosmosConnectionString::getConnectionString);

        final CosmosConnectionString cosmosUrlConnectionString
                = fromConnectionString(TEST_COSMOS_URL_CONNECTION_STRING_1);

        assertThat(cosmosUrlConnectionString)
                .returns(Optional.of("https://ech-0a9d975b:8080"), CosmosConnectionString::getAccountEndpoint)
                .returns(Optional.of("C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw=="), CosmosConnectionString::getAccountKey)
                .returns(Optional.of("testdb1"), CosmosConnectionString::getDatabaseName)
                .returns(Optional.of("false"), c -> c.getProperty("ssl"))
                .returns(Optional.empty(), c -> c.getProperty("notExisting"))
                .returns(TEST_COSMOS_URL_CONNECTION_STRING_1, CosmosConnectionString::getConnectionString);
    }
}