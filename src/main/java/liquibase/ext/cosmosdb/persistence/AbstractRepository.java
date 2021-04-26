package liquibase.ext.cosmosdb.persistence;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.*;
import liquibase.ext.cosmosdb.statement.JsonUtils;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.Constants.Properties.ID;

public abstract class AbstractRepository<T> {

    @Getter
    private final CosmosContainer container;

    @Getter
    private final AbstractItemToDocumentConverter<T, Map<String, Object>> converter;

    public AbstractRepository(final CosmosContainer container, final AbstractItemToDocumentConverter<T, Map<String, Object>> converter) {
        this.container = container;
        this.converter = converter;
    }

    public Optional<T> get(final String id) {

        final SqlQuerySpec querySpec
                = new SqlQuerySpec("SELECT * FROM c WHERE c.id=" + JsonUtils.COSMOS_ID_PARAMETER, new SqlParameter(JsonUtils.COSMOS_ID_PARAMETER, id));
        return container.queryItems(querySpec, null, Map.class).stream().findFirst().map(converter::fromDocument);
    }

    public List<T> getAll() {
        return readAllItems().stream().map(converter::fromDocument).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> readAllItems() {

        return container.queryItems("SELECT * FROM c", null, Map.class).stream()
                .map(i -> (Map<String, Object>)i).collect(Collectors.toList());
    }

    public int create(final Map<String, Object> document) {
        container.createItem(document, PartitionKey.NONE, null);
        return 1;
    }

    public int create(final T item) {
        final Map<String, Object> document = converter.toDocument(item);
        return create(document);
    }


    public int replace(final Map<String, Object> document) {
        container.replaceItem(document, (String)document.get(ID), PartitionKey.NONE,  null);
        return 1;
    }

    public int replace(T item) {
        final Map<String, Object> document = converter.toDocument(item);
        return replace(document);
    }

    public int upsert(Map<String, Object> document) {
        container.upsertItem(document, null);
        return 1;
    }

    public int upsert(final T item) {
        final Map<String, Object> document = converter.toDocument(item);
        return upsert(document);
    }

    public int delete(final Map<String, Object> document) {
        container.deleteItem((String)document.get(ID), PartitionKey.NONE,  null);
        return 1;
    }

    public int delete(final T item) {
        final Map<String, Object> document = converter.toDocument(item);
        return delete(document);
    }

    public boolean exists(final String id) {
        final SqlQuerySpec querySpec
                = new SqlQuerySpec("SELECT * FROM c WHERE c.id=" + JsonUtils.COSMOS_ID_PARAMETER, new SqlParameter(JsonUtils.COSMOS_ID_PARAMETER, id));

        return 1L == container.queryItems(querySpec, null, Map.class).stream().count();
    }
}
