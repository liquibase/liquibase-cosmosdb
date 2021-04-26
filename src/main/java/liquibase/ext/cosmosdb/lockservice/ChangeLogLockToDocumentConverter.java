package liquibase.ext.cosmosdb.lockservice;

import liquibase.ext.cosmosdb.persistence.AbstractItemToDocumentConverter;

import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.Constants.Properties.ID;
import static java.util.Optional.ofNullable;

public class ChangeLogLockToDocumentConverter extends AbstractItemToDocumentConverter<CosmosChangeLogLock, Map<String, Object>> {
    @Override
    public Map<String, Object> toDocument(final CosmosChangeLogLock item) {
        //TODO: Find a solution to use Document from azure instead of Map
        // Changed from Document as it fails when doing replace, upsert with :
        // Cosmos DB Error: PartitionKey extracted from document doesn't match the one specified in the header
//        final Document document = new Document();
//        document.setId(Integer.toString(item.getId()));
//        document.set(CosmosChangeLogLock.Fields.lockGranted, fromDate(item.getLockGranted()));
//        document.set(CosmosChangeLogLock.Fields.lockedBy, item.getLockedBy());
//        document.set(CosmosChangeLogLock.Fields.locked, item.getLocked());
//        document.set(CosmosChangeLogLock.Fields.partition, item.getPartition());

        final Map<String, Object> document = new HashMap<>();
        document.put(ID, Integer.toString(item.getId()));
        document.put(CosmosChangeLogLock.Fields.lockGranted, fromDate(item.getLockGranted()));
        document.put(CosmosChangeLogLock.Fields.lockedBy, item.getLockedBy());
        document.put(CosmosChangeLogLock.Fields.locked, item.getLocked());

        return document;
    }

    @Override
    public CosmosChangeLogLock fromDocument(final Map<String, Object> document) {
        return CosmosChangeLogLock.builder()
                .id(ofNullable(document.get(ID)).map(s -> Integer.parseInt((String) s)).orElse(-1))
                .lockGranted(ofNullable(document.get(CosmosChangeLogLock.Fields.lockGranted)).map(s -> toDate((String) s)).orElse(null))
                .lockedBy(ofNullable((String) document.get(CosmosChangeLogLock.Fields.lockedBy)).orElse(""))
                .locked((Boolean) ofNullable(document.get(CosmosChangeLogLock.Fields.locked)).orElse(null))
                .build();
    }
}
