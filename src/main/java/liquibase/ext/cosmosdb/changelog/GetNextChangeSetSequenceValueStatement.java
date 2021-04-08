package liquibase.ext.cosmosdb.changelog;

import liquibase.ext.cosmosdb.statement.CountDocumentsInContainerStatement;

public class GetNextChangeSetSequenceValueStatement extends CountDocumentsInContainerStatement {

    public static final String COMMAND_NAME = "nextChangeSetSequence";

    public GetNextChangeSetSequenceValueStatement(final String containerId) {
        super(containerId);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

}
