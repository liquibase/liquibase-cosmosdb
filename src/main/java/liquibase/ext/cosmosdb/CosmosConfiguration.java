package liquibase.ext.cosmosdb;

/*-
 * #%L
 * Liquibase CosmosDB Extension
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

import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;

public class CosmosConfiguration implements AutoloadedConfigurations {

    public static final String NAMESPACE = "liquibase.cosmosdb";

    public static final ConfigurationDefinition<Boolean> INFER_PARTITION_KEY_KIND;

    static {
        INFER_PARTITION_KEY_KIND = new ConfigurationDefinition.Builder(NAMESPACE)
                .define("inferPartitionKeyKind", Boolean.class)
                .setDescription("When a container's partition key supplies paths but no kind, infer HASH "
                        + "(single path) or MULTI_HASH (hierarchical) before creating or replacing the container. "
                        + "Defaults false to preserve the behavior of prior releases. Enable it for the vNext "
                        + "emulator, which leaves a missing kind null and otherwise faults on later interactions.")
                .setDefaultValue(Boolean.FALSE)
                .build();
    }
}
