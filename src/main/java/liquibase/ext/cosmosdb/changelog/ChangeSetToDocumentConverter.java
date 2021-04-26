package liquibase.ext.cosmosdb.changelog;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;
import liquibase.ext.cosmosdb.persistence.AbstractItemToDocumentConverter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.Constants.Properties.ID;
import static java.util.Optional.ofNullable;
import static liquibase.sqlgenerator.core.MarkChangeSetRanGenerator.*;

public class ChangeSetToDocumentConverter extends AbstractItemToDocumentConverter<CosmosRanChangeSet, Map<String, Object>> {

    @Override
    public Map<String, Object> toDocument(final CosmosRanChangeSet item) {

        final Map<String, Object> document = new HashMap<>();
        // "id" is an internal String field required for CosmosContainer
        // will populate with Random UUID String thus being unique
        document.put(ID, item.getUuid());
        // The ChangeSet.getId() is populated to "changeSetId" field
        document.put(CosmosRanChangeSet.Fields.FILE_NAME, item.getChangeLog());
        document.put(CosmosRanChangeSet.Fields.CHANGE_SET_ID, item.getId());
        document.put(CosmosRanChangeSet.Fields.AUTHOR, item.getAuthor());
        document.put(CosmosRanChangeSet.Fields.LAST_CHECK_SUM,
                ofNullable(item.getLastCheckSum()).map(CheckSum::toString).orElse(null));
        document.put(CosmosRanChangeSet.Fields.DATE_EXECUTED,
                ofNullable(item.getDateExecuted()).map(this::fromDate).orElse(null));
        document.put(CosmosRanChangeSet.Fields.TAG, item.getTag());
        document.put(CosmosRanChangeSet.Fields.EXEC_TYPE,
                ofNullable(item.getExecType()).map(e -> e.value).orElse(null));
        document.put(CosmosRanChangeSet.Fields.DESCRIPTION, item.getDescription());
        document.put(CosmosRanChangeSet.Fields.COMMENTS, item.getComments());
        document.put(CosmosRanChangeSet.Fields.CONTEXT_EXPRESSION, buildFullContext(item.getContextExpression(), item.getInheritableContexts()));
        document.put(CosmosRanChangeSet.Fields.LABELS, buildLabels(item.getLabels()));
        document.put(CosmosRanChangeSet.Fields.DEPLOYMENT_ID, item.getDeploymentId());
        document.put(CosmosRanChangeSet.Fields.ORDER_EXECUTED, item.getOrderExecuted());
        document.put(CosmosRanChangeSet.Fields.LIQUIBASE, item.getLiquibase());

        return document;
    }

    @Override
    public CosmosRanChangeSet fromDocument(final Map<String, Object> document) {

        return new CosmosRanChangeSet(
                // Internal id which is populated with UUID
                (String) document.get(ID),
                (String) document.get(CosmosRanChangeSet.Fields.FILE_NAME),
                // Change Set Id which is populated to id POJO field
                (String) document.get(CosmosRanChangeSet.Fields.CHANGE_SET_ID),
                (String) document.get(CosmosRanChangeSet.Fields.AUTHOR),
                CheckSum.parse((String) document.get(CosmosRanChangeSet.Fields.LAST_CHECK_SUM)),
                toDate((String) document.get(CosmosRanChangeSet.Fields.DATE_EXECUTED)),
                (String) document.get(CosmosRanChangeSet.Fields.TAG),
                ofNullable(document.get(CosmosRanChangeSet.Fields.EXEC_TYPE))
                        .map(s -> ChangeSet.ExecType.valueOf((String) s)).orElse(null),
                (String) document.get(CosmosRanChangeSet.Fields.DESCRIPTION),
                (String) document.get(CosmosRanChangeSet.Fields.COMMENTS),
                new ContextExpression((String)document.get(CosmosRanChangeSet.Fields.CONTEXT_EXPRESSION)),
                // TODO: parse in and out
                null,
                new Labels((String)document.get(CosmosRanChangeSet.Fields.LABELS)),
                (String) document.get(CosmosRanChangeSet.Fields.DEPLOYMENT_ID),
                (Integer) ofNullable(document.get(CosmosRanChangeSet.Fields.ORDER_EXECUTED)).orElse(null),
                (String) document.get(CosmosRanChangeSet.Fields.LIQUIBASE)
        );
    }

    public String buildLabels(Labels labels) {
        if (labels == null || labels.isEmpty()) {
            return null;
        }
        return labels.toString();
    }

    public String buildFullContext(final ContextExpression contextExpression, final Collection<ContextExpression> inheritableContexts) {
        if ((contextExpression == null) || contextExpression.isEmpty()) {
            return null;
        }

        StringBuilder contextExpressionString = new StringBuilder();
        boolean notFirstContext = false;
        for (ContextExpression inheritableContext : inheritableContexts) {
            appendContext(contextExpressionString, inheritableContext.toString(), notFirstContext);
            notFirstContext = true;
        }
        appendContext(contextExpressionString, contextExpression.toString(), notFirstContext);

        return contextExpressionString.toString();
    }

    private void appendContext(StringBuilder contextExpression, String contextToAppend, boolean notFirstContext) {
        boolean complexExpression = contextToAppend.contains(COMMA) || contextToAppend.contains(WHITESPACE);
        if (notFirstContext) {
            contextExpression.append(AND);
        }
        if (complexExpression) {
            contextExpression.append(OPEN_BRACKET);
        }
        contextExpression.append(contextToAppend);
        if (complexExpression) {
            contextExpression.append(CLOSE_BRACKET);
        }
    }
}
