package liquibase.ext.cosmosdb.database;

import com.azure.cosmos.CosmosDatabase;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static com.azure.cosmos.implementation.apachecommons.lang.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CosmosConnectionTest {

    public static final String TEST_COSMOS_JSON_CONNECTION_STRING_1 = "cosmosdb://{\"accountEndpoint\":\"https://ech-0a9d975b:8080\",\"accountKey\":\"C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==\",\"databaseName\":\"testdb1\"}";

    public static final String TEST_COSMOS_URL_CONNECTION_STRING_1 = "cosmosdb://ech-0a9d975b:C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==:8080/testdb1?ssl=false";


    @Mock
    private CosmosClientDriver driverMock;

    @Mock
    private CosmosClientProxy clientMock;

    @Mock
    private CosmosDatabase databaseMock;

    @Mock
    private Properties propertiesMock;

    @SneakyThrows
    @Test
    void getURLTest() {
        CosmosConnection connection = new CosmosConnection();
        assertThat(connection.getURL()).isEqualTo(EMPTY);

        when(driverMock.connect(any())).thenReturn(clientMock);
        connection.open(TEST_COSMOS_JSON_CONNECTION_STRING_1, driverMock, propertiesMock);
        assertThat(connection.getURL()).isEqualTo(TEST_COSMOS_JSON_CONNECTION_STRING_1);
    }

    @SneakyThrows
    @Test
    void openCloseTest() {
        CosmosConnection connection = new CosmosConnection();
        assertThat(connection.getCosmosDatabase()).isNull();
        assertThat(connection.getCosmosClient()).isNull();
        assertThat(connection.isClosed()).isTrue();

        when(driverMock.connect(any())).thenReturn(clientMock);
        when(clientMock.getDatabase(any())).thenReturn(databaseMock);
        connection.open(TEST_COSMOS_JSON_CONNECTION_STRING_1, driverMock, propertiesMock);
        assertThat(connection.getCosmosDatabase()).isNotNull();
        assertThat(connection.getCosmosClient()).isNotNull();
        assertThat(connection.isClosed()).isFalse();

        connection.close();
        assertThat(connection.getCosmosDatabase()).isNull();
        assertThat(connection.getCosmosClient()).isNull();
        assertThat(connection.isClosed()).isTrue();
    }

    @Test
    @SneakyThrows
    void shouldNotCloseResetIfAlreadyClosed() {
        CosmosConnection connection = new CosmosConnection();

        when(driverMock.connect(any())).thenReturn(clientMock);
        when(clientMock.getDatabase(any())).thenReturn(databaseMock);
        connection.open(TEST_COSMOS_JSON_CONNECTION_STRING_1, driverMock, propertiesMock);

        connection.close();

        assertThatCode(connection::close).doesNotThrowAnyException();
    }
}