package liquibase.ext.cosmosdb.changelog;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.implementation.BadRequestException;
import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;
import liquibase.ext.cosmosdb.AbstractCosmosWithConnectionIntegrationTest;
import liquibase.ext.cosmosdb.lockservice.CreateChangeLogLockContainerStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ChangeSetRepositoryIT extends AbstractCosmosWithConnectionIntegrationTest {

    public static final String CONTAINER_NAME_1 = "containerName1Log";

    public static final String UUID_1 = "uuid1";
    public static final String UUID_2 = "uuid2";

    public ChangeSetRepository repository;

    public CosmosContainer container;

    protected CosmosRanChangeSet minimal;

    protected CosmosRanChangeSet maximal;

    protected Date date1 = new Date();

    protected CheckSum checkSum1 = CheckSum.compute("CheckSumString");

    protected Labels labels1 = new Labels("Label1", "Label2");

    protected ContextExpression contextExpression1 = new ContextExpression("context1", "context2");

    protected Collection<ContextExpression> inheritableContexts1 =
            Collections.singletonList(new ContextExpression("inheritableContext1", "inheritableContext2"));

    @BeforeEach
    protected void setUpEach() {
        super.setUpEach();
        new CreateChangeLogLockContainerStatement(CONTAINER_NAME_1).execute(database);
        repository = new ChangeSetRepository(cosmosDatabase, CONTAINER_NAME_1);
        container = repository.getContainer();

        minimal = new CosmosRanChangeSet(
                null
                , null
                , null
                , null
                , null
                , null
                , null
                , null
                , null
                , null
                , null
                , null
                , null
                , null
                , null
                , null
        );

        maximal = new CosmosRanChangeSet(
                UUID_2
                , "FilePath"
                , "ChangeSetId"
                , "Author"
                , checkSum1
                , date1
                , "Tag"
                , ChangeSet.ExecType.EXECUTED
                , "Description"
                , "Comments"
                , contextExpression1
                , inheritableContexts1
                , labels1
                , "DeploymentId"
                , 1
                , "BuildVersion"
        );

    }

    @Test
    void testGet() {
        //missing id
        assertThatExceptionOfType(BadRequestException.class).isThrownBy(() -> repository.create(minimal));
        assertThat(repository.get(UUID_1)).isEmpty();
        minimal.setUuid(UUID_1);
        final int rowsAffected1 = repository.create(minimal);
        assertThat(rowsAffected1).isEqualTo(1);

        final CosmosRanChangeSet actual1 = repository.get(UUID_1).orElse(null);
        assertThat(actual1).isNotNull();
        assertThat(actual1.getUuid()).isEqualTo(UUID_1);

        final int rowsAffected2 = repository.create(maximal);
        assertThat(rowsAffected2).isEqualTo(1);
        final CosmosRanChangeSet actual2 = repository.get(maximal.getUuid()).orElse(null);
        assertThat(actual2).isNotNull();
        assertThat(actual2.getUuid()).isEqualTo(UUID_2);
        assertThat(actual2.getChangeLog()).isEqualTo("FilePath");
        assertThat(actual2.getId()).isEqualTo("ChangeSetId");
        assertThat(actual2.getAuthor()).isEqualTo("Author");
        assertThat(actual2.getLastCheckSum()).isEqualTo(checkSum1);
        assertThat(actual2.getDateExecuted()).isEqualTo(date1);
        assertThat(actual2.getTag()).isEqualTo("Tag");
        assertThat(actual2.getExecType()).isEqualTo(ChangeSet.ExecType.EXECUTED);
        assertThat(actual2.getDescription()).isEqualTo("Description");
        assertThat(actual2.getComments()).isEqualTo("Comments");
        assertThat(actual2.getContextExpression().getContexts()).hasSize(3);
        // Cannot get from DB as it is pass through from ChangeSet
        assertThat(actual2.getInheritableContexts()).isNull();
        assertThat(actual2.getLabels().getLabels()).containsAll(labels1.getLabels());
        assertThat(actual2.getDeploymentId()).isEqualTo("DeploymentId");
        assertThat(actual2.getOrderExecuted()).isEqualTo(1);
        assertThat(actual2.getLiquibase()).isEqualTo("BuildVersion");
    }

    @Test
    void testGetAll() {

        assertThat(repository.getAll().size()).isEqualTo(0);

        maximal.setUuid(UUID_1);
        repository.create(maximal);
        List<CosmosRanChangeSet> ranChangeSets1 = repository.getAll();
        assertThat(ranChangeSets1.size()).isEqualTo(1);
        assertThat(ranChangeSets1.stream().map(CosmosRanChangeSet::getUuid).filter(UUID_1::equals).findFirst()).isPresent();

        maximal.setUuid(UUID_2);
        repository.create(maximal);
        List<CosmosRanChangeSet> ranChangeSets2 = repository.getAll();
        assertThat(ranChangeSets2.size()).isEqualTo(2);
        assertThat(ranChangeSets1.stream().map(CosmosRanChangeSet::getUuid).filter(UUID_1::equals).findFirst()).isPresent();
        assertThat(ranChangeSets2.stream().map(CosmosRanChangeSet::getUuid).filter(UUID_2::equals).findFirst()).isPresent();
    }

    @Test
    void testCreate() {
        //missing id
        assertThatExceptionOfType(BadRequestException.class).isThrownBy(() -> repository.create(minimal));
        assertThat(repository.get(UUID_1)).isNotPresent();
        minimal.setUuid(UUID_1);
        int rowsAffected1 = repository.create(minimal);
        assertThat(rowsAffected1).isEqualTo(1);

        final CosmosRanChangeSet actual1 = repository.get(UUID_1).orElse(null);
        assertThat(actual1).isNotNull();
        assertThat(actual1.getUuid()).isEqualTo(UUID_1);
    }

    @Test
    void testReplace() {
    }

    @Test
    void testUpsert() {
    }

    @Test
    void testDelete() {
    }

    @Test
    void testExists() {
    }

    @Test
    void testCount() {
    }

    @Test
    void testGetContainer() {
    }

    @Test
    void testGetConverter() {
    }
}