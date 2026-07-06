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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CosmosConfigurationTest {

    @Test
    void testInferPartitionKeyKindKey() {
        assertThat(CosmosConfiguration.INFER_PARTITION_KEY_KIND.getKey())
                .isEqualTo("liquibase.cosmosdb.inferPartitionKeyKind");
    }

    @Test
    void testInferPartitionKeyKindDefaultsToFalse() {
        assertThat(CosmosConfiguration.INFER_PARTITION_KEY_KIND.getDefaultValue()).isFalse();
    }
}
