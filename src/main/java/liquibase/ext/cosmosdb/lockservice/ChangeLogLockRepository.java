package liquibase.ext.cosmosdb.lockservice;

import com.azure.cosmos.CosmosDatabase;
import liquibase.ext.cosmosdb.persistence.AbstractRepository;

public class ChangeLogLockRepository extends AbstractRepository<CosmosChangeLogLock> {

    public ChangeLogLockRepository(final CosmosDatabase database, final String containerId) {
        super(database.getContainer(containerId)
                , new ChangeLogLockToDocumentConverter());
    }
}
