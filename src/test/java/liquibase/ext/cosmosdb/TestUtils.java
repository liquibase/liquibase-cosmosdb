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

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.LiquibaseException;
import liquibase.ext.cosmosdb.database.CosmosLiquibaseDatabase;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Properties;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class TestUtils {
    public static final String APPLICATION_TEST_PROPERTIES_FILE_NAME = "application-test.properties";
    public static final String DB_CONNECTION_URI_PROPERTY = "db.connection.uri";
    public static final String DATABASE_CHANGE_LOG_TABLE_NAME = "DATABASECHANGELOG";
    public static final String DATABASE_CHANGE_LOG_LOCK_TABLE_NAME = "DATABASECHANGELOGLOCK";

    @SneakyThrows
    public static Properties loadProperties() {
        return loadProperties(APPLICATION_TEST_PROPERTIES_FILE_NAME);
    }

    @SneakyThrows
    public static Properties loadProperties(final String propertyFile) {
        final Properties properties = new Properties();
        properties.load(TestUtils.class.getClassLoader().getResourceAsStream(propertyFile));

        final String connectionUriOverride = System.getProperty(DB_CONNECTION_URI_PROPERTY);
        if (connectionUriOverride != null) {
            properties.setProperty(DB_CONNECTION_URI_PROPERTY, connectionUriOverride);
        }

        return properties;
    }

    public static List<ChangeSet> getChangeSets(final String changeSetPath, final CosmosLiquibaseDatabase database) throws LiquibaseException {
        final ClassLoaderResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
        final ChangeLogParser parser =
            ChangeLogParserFactory.getInstance().getParser(getExtension(changeSetPath), resourceAccessor);

        final DatabaseChangeLog changeLog =
            parser.parse(changeSetPath, new ChangeLogParameters(database), resourceAccessor);
        return changeLog.getChangeSets();
    }

    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        }
        int extensionPos = filename.lastIndexOf(".");
        int lastSeparator = indexOfLastSeparator(filename);
        return ((lastSeparator > extensionPos) ? -1 : extensionPos);
    }

    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf('/');
        int lastWindowsPos = filename.lastIndexOf('/');
        return Math.max(lastUnixPos, lastWindowsPos);
    }
}
