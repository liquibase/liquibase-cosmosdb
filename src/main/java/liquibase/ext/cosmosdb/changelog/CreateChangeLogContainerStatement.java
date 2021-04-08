package liquibase.ext.cosmosdb.changelog;

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

import com.azure.cosmos.implementation.DocumentCollection;
import liquibase.ext.cosmosdb.statement.CreateContainerStatement;
import liquibase.ext.cosmosdb.statement.JsonUtils;

public class CreateChangeLogContainerStatement extends CreateContainerStatement {

    public static final String COMMAND_NAME = "createChangeLogContainer";

    /**
     * See {@link DocumentCollection}. Will be parsed from json apart from id.
     * Container id will be populated from {@link CreateChangeLogContainerStatement#getContainerId()}
     */
    private static final String OPTIONS = String.format("{  \n" +
                    "  \"indexingPolicy\": {  \n" +
                    "    \"automatic\": true,  \n" +
                    "    \"indexingMode\": \"Consistent\",  \n" +
                    "    \"includedPaths\": [  \n" +
                    "      {  \n" +
                    "        \"path\": \"/*\",  \n" +
                    "        \"indexes\": [  \n" +
                    "          {  \n" +
                    "            \"dataType\": \"String\",  \n" +
                    "            \"precision\": -1,  \n" +
                    "            \"kind\": \"Range\"  \n" +
                    "          }  \n" +
                    "        ]  \n" +
                    "      }  \n" +
                    "    ]  \n" +
                    "  },  \n" +
                    //partition key
                    "  \"partitionKey\": {  \n" +
                    "    \"paths\": [  \n" +
                    "      \"%s\"  \n" +
                    "    ]  \n" +
                    "  },   \n" +
                    "  \"uniqueKeyPolicy\": {\n" +
                    "        \"uniqueKeys\": [\n" +
                    "          {\n" +
                    "            \"paths\": [\n" +
                    //unique keys
                    "              \"/%s\", \"/%s\", \"/%s\"\n" +
                    "            ]\n" +
                    "          }\n" +
                    "        ]\n" +
                    "  },  \n" +
                    "}  ",
            JsonUtils.DEFAULT_PARTITION_KEY_PATH,
            CosmosRanChangeSet.Fields.FILE_NAME,
            CosmosRanChangeSet.Fields.AUTHOR,
            CosmosRanChangeSet.Fields.CHANGE_SET_ID);

    public CreateChangeLogContainerStatement(final String containerId) {
        super(containerId, OPTIONS);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }
}
