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


import liquibase.ext.cosmosdb.database.CosmosClientDriver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.util.Properties;

import static liquibase.ext.cosmosdb.TestUtils.DB_CONNECTION_URI_PROPERTY;
import static liquibase.ext.cosmosdb.TestUtils.loadProperties;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractCosmosIntegrationTest {

    public static final String CONTAINER_NAME_PERSON = "person";
    public static final String PARTITION_KEY_PATH_LAST_NAME = "{ \"partitionKey\": {\"paths\": [\"/lastName\"], \"kind\": \"Hash\" } }";

    protected Properties testProperties;
    protected String connectionString;
    protected CosmosClientDriver driver;
    protected Properties driverProperties;

    @BeforeAll
    protected void setUp() {
        testProperties = loadProperties();
        connectionString = testProperties.getProperty(DB_CONNECTION_URI_PROPERTY);
        driver = new CosmosClientDriver();
        driverProperties = new Properties();
    }

}
