package liquibase.ext.cosmosdb.database;

import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static liquibase.ext.cosmosdb.TestUtils.DATABASE_CHANGE_LOG_LOCK_TABLE_NAME;
import static liquibase.ext.cosmosdb.TestUtils.DATABASE_CHANGE_LOG_TABLE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

class CosmosLiquibaseDatabaseTest {

    @BeforeEach
    void setUp() {
        DatabaseFactory.reset();
    }

    @AfterEach
    void tearDown() {
        DatabaseFactory.reset();
    }

    @Test
    void getName() {
    }

    @Test
    void requiresPassword() {
    }

    @Test
    void requiresUsername() {
    }

    @Test
    void getContainingObjects() {
    }

    @Test
    void getConnection() {
    }

    @Test
    void setConnection() {
    }

    @Test
    void getAutoCommitMode() {
    }

    @Test
    void addReservedWords() {
    }

    @Test
    void supportsDDLInTransaction() {
    }

    @Test
    void getDatabaseProductName() {
    }

    @Test
    void getDatabaseProductVersion() {
    }

    @Test
    void getDatabaseMajorVersion() {
    }

    @Test
    void getDatabaseMinorVersion() {
    }

    @Test
    void getDefaultCatalogName() {
    }

    @Test
    void setDefaultCatalogName() {
    }

    @Test
    void getConnectionCatalogName() {
    }

    @Test
    void correctSchema() {
    }

    @Test
    void testCorrectSchema() {
    }

    @Test
    void correctObjectName() {
    }

    @Test
    void getDefaultSchema() {
    }

    @Test
    void getDefaultSchemaName() {
    }

    @Test
    void getDefaultScaleForNativeDataType() {
    }

    @Test
    void setDefaultSchemaName() {
    }

    @Test
    void getConnectionSchemaName() {
    }

    @Test
    void getConnectionSchemaNameCallStatement() {
    }

    @Test
    void getFetchSize() {
    }

    @Test
    void getSystemTables() {
    }

    @Test
    void getSystemViews() {
    }

    @Test
    void supportsSequences() {
    }

    @Test
    void supportsAutoIncrement() {
    }

    @Test
    void getDateLiteral() {
    }

    @Test
    void getDateTimeLiteral() {
    }

    @Test
    void testGetDateLiteral() {
    }

    @Test
    void getTimeLiteral() {
    }

    @Test
    void testGetDateLiteral1() {
    }

    @Test
    void parseDate() {
    }

    @Test
    void isDateOnly() {
    }

    @Test
    void isDateTime() {
    }

    @Test
    void isTimestamp() {
    }

    @Test
    void isTimeOnly() {
    }

    @Test
    void getLineComment() {
    }

    @Test
    void getAutoIncrementClause() {
    }

    @Test
    void testGetAutoIncrementClause() {
    }

    @Test
    void testGetAutoIncrementClause1() {
    }

    @Test
    void generateAutoIncrementStartWith() {
    }

    @Test
    void generateAutoIncrementBy() {
    }

    @Test
    void getAutoIncrementOpening() {
    }

    @Test
    void getAutoIncrementClosing() {
    }

    @Test
    void getAutoIncrementStartWithClause() {
    }

    @Test
    void getAutoIncrementByClause() {
    }

    @Test
    void getConcatSql() {
    }

    @Test
    void getDatabaseChangeLogTableName() {
    }

    @Test
    void setDatabaseChangeLogTableName() {
        final CosmosLiquibaseDatabase cosmosLiquibaseDatabase= new CosmosLiquibaseDatabase();
        assertThat(cosmosLiquibaseDatabase.getDatabaseChangeLogTableName()).isEqualTo(DATABASE_CHANGE_LOG_TABLE_NAME);
    }

    @Test
    void getDatabaseChangeLogLockTableName() {
        final CosmosLiquibaseDatabase cosmosLiquibaseDatabase= new CosmosLiquibaseDatabase();
        assertThat(cosmosLiquibaseDatabase.getDatabaseChangeLogLockTableName()).isEqualTo(DATABASE_CHANGE_LOG_LOCK_TABLE_NAME);
    }

    @Test
    void setDatabaseChangeLogLockTableName() {
    }

    @Test
    void getLiquibaseTablespaceName() {
    }

    @Test
    void setLiquibaseTablespaceName() {
    }

    @Test
    void canCreateChangeLogTable() {
    }

    @Test
    void setCanCacheLiquibaseTableInfo() {
    }

    @Test
    void getLiquibaseCatalogName() {
    }

    @Test
    void setLiquibaseCatalogName() {
    }

    @Test
    void getLiquibaseSchemaName() {
    }

    @Test
    void setLiquibaseSchemaName() {
    }

    @Test
    void isCaseSensitive() {
    }

    @Test
    void setCaseSensitive() {
    }

    @Test
    void isReservedWord() {
    }

    @Test
    void startsWithNumeric() {
    }

    @Test
    void dropDatabaseObjects() {
    }

    @Test
    void supportsDropTableCascadeConstraints() {
    }

    @Test
    void isSystemObject() {
    }

    @Test
    void isSystemView() {
    }

    @Test
    void isLiquibaseObject() {
    }

    @Test
    void tag() {
    }

    @Test
    void doesTagExist() {
    }

    @Test
    void testToString() {
    }

    @Test
    void getViewDefinition() {
    }

    @Test
    void escapeTableName() {
    }

    @Test
    void escapeObjectName() {
    }

    @Test
    void testEscapeObjectName() {
    }

    @Test
    void mustQuoteObjectName() {
    }

    @Test
    void getQuotingStartCharacter() {
    }

    @Test
    void getQuotingEndCharacter() {
    }

    @Test
    void getQuotingEndReplacement() {
    }

    @Test
    void quoteObject() {
    }

    @Test
    void escapeIndexName() {
    }

    @Test
    void escapeSequenceName() {
    }

    @Test
    void escapeConstraintName() {
    }

    @Test
    void escapeColumnName() {
    }

    @Test
    void testEscapeColumnName() {
    }

    @Test
    void escapeColumnNameList() {
    }

    @Test
    void supportsSchemas() {
    }

    @Test
    void supportsCatalogs() {
    }

    @Test
    void jdbcCallsCatalogsSchemas() {
    }

    @Test
    void supportsCatalogInObjectName() {
    }

    @Test
    void generatePrimaryKeyName() {
    }

    @Test
    void escapeViewName() {
    }

    @Test
    void getRunStatus() {
    }

    @Test
    void getRanChangeSet() {
    }

    @Test
    void getRanChangeSetList() {
    }

    @Test
    void getRanDate() {
    }

    @Test
    void markChangeSetExecStatus() {
    }

    @Test
    void removeRanStatus() {
    }

    @Test
    void escapeStringForDatabase() {
    }

    @Test
    void commit() {
    }

    @Test
    void rollback() {
    }

    @Test
    void testEquals() {
    }

    @Test
    void testHashCode() {
    }

    @Test
    void close() {
    }

    @Test
    void supportsRestrictForeignKeys() {
    }

    @Test
    void isAutoCommit() {
    }

    @Test
    void setAutoCommit() {
    }

    @Test
    void isSafeToRunUpdate() {
    }

    @Test
    void executeStatements() {
    }

    @Test
    void execute() {
    }

    @Test
    void saveStatements() {
    }

    @Test
    void executeRollbackStatements() {
    }

    @Test
    void testExecuteRollbackStatements() {
    }

    @Test
    void saveRollbackStatement() {
    }

    @Test
    void filterRollbackVisitors() {
    }

    @Test
    void getDateFunctions() {
    }

    @Test
    void isFunction() {
    }

    @Test
    void resetInternalState() {
    }

    @Test
    void supportsForeignKeyDisable() {
    }

    @Test
    void disableForeignKeyChecks() {
    }

    @Test
    void enableForeignKeyChecks() {
    }

    @Test
    void createsIndexesForForeignKeys() {
    }

    @Test
    void getDataTypeMaxParameters() {
    }

    @Test
    void getSchemaFromJdbcInfo() {
    }

    @Test
    void getJdbcCatalogName() {
    }

    @Test
    void getJdbcSchemaName() {
    }

    @Test
    void testGetJdbcCatalogName() {
    }

    @Test
    void testGetJdbcSchemaName() {
    }

    @Test
    void dataTypeIsNotModifiable() {
    }

    @Test
    void getObjectQuotingStrategy() {
    }

    @Test
    void setObjectQuotingStrategy() {
    }

    @Test
    void generateDatabaseFunctionValue() {
    }

    @Test
    void getCurrentDateTimeFunction() {
    }

    @Test
    void setCurrentDateTimeFunction() {
    }

    @Test
    void isDefaultSchema() {
    }

    @Test
    void isDefaultCatalog() {
    }

    @Test
    void getOutputDefaultSchema() {
    }

    @Test
    void setOutputDefaultSchema() {
    }

    @Test
    void getOutputDefaultCatalog() {
    }

    @Test
    void setOutputDefaultCatalog() {
    }

    @Test
    void supportsPrimaryKeyNames() {
    }

    @Test
    void getSystemSchema() {
    }

    @Test
    void escapeDataTypeName() {
    }

    @Test
    void unescapeDataTypeName() {
    }

    @Test
    void unescapeDataTypeString() {
    }

    @Test
    void get() {
    }

    @Test
    void set() {
    }

    @Test
    void validate() {
    }

    @Test
    void getMaxFractionalDigitsForTimestamp() {
    }

    @Test
    void getDefaultFractionalDigitsForTimestamp() {
    }

    @Test
    void supportsBatchUpdates() {
    }

    @Test
    void supportsNotNullConstraintNames() {
    }

    @Test
    void requiresExplicitNullForColumns() {
    }

    @Test
    void getSchemaAndCatalogCase() {
    }

    @Test
    void testGetDatabaseChangeLogTableName() {
    }

    @Test
    void testSetDatabaseChangeLogTableName() {
    }

    @Test
    void testGetDatabaseChangeLogLockTableName() {
    }

    @Test
    void getShortName() {
    }

    @Test
    void testGetDefaultCatalogName() {
    }

    @Test
    void testSetDefaultCatalogName() {
    }

    @Test
    void testGetDefaultSchemaName() {
    }

    @Test
    void testSetDefaultSchemaName() {
    }

    @Test
    void testGetDefaultScaleForNativeDataType() {
    }

    @Test
    void getDefaultPort() {
    }

    @Test
    void testGetFetchSize() {
    }

    @Test
    void testGetLiquibaseCatalogName() {
    }

    @Test
    void supportsInitiallyDeferrableColumns() {
    }

    @Test
    void testSupportsSequences() {
    }

    @Test
    void testSupportsDropTableCascadeConstraints() {
    }

    @Test
    void testSupportsAutoIncrement() {
    }

    @Test
    void testGetLineComment() {
    }

    @Test
    void testGetAutoIncrementClause2() {
    }

    @Test
    void testIsSystemObject() {
    }

    @Test
    void testIsLiquibaseObject() {
    }

    @Test
    void testGetViewDefinition() {
    }

    @Test
    void testEscapeObjectName1() {
    }

    @Test
    void testEscapeTableName() {
    }

    @Test
    void testEscapeIndexName() {
    }

    @Test
    void testEscapeObjectName2() {
    }

    @Test
    void testEscapeColumnName1() {
    }

    @Test
    void testEscapeColumnName2() {
    }

    @Test
    void testEscapeColumnNameList() {
    }

    @Test
    void supportsTablespaces() {
    }

    @Test
    void testSupportsCatalogs() {
    }

    @Test
    void testGetSchemaAndCatalogCase() {
    }

    @Test
    void testSupportsSchemas() {
    }

    @Test
    void testSupportsCatalogInObjectName() {
    }

    @Test
    void testGeneratePrimaryKeyName() {
    }

    @Test
    void testEscapeSequenceName() {
    }

    @Test
    void testEscapeViewName() {
    }

    @Test
    void testCommit() {
    }

    @Test
    void testRollback() {
    }

    @Test
    void testEscapeStringForDatabase() {
    }

    @Test
    void testClose() {
    }

    @Test
    void testSupportsRestrictForeignKeys() {
    }

    @Test
    void testEscapeConstraintName() {
    }

    @Test
    void testIsSafeToRunUpdate() {
    }

    @Test
    void testSaveStatements() {
    }

    @Test
    void testExecuteRollbackStatements1() {
    }

    @Test
    void testExecuteRollbackStatements2() {
    }

    @Test
    void testSaveRollbackStatement() {
    }

    @Test
    void testGetDateFunctions() {
    }

    @Test
    void testSupportsForeignKeyDisable() {
    }

    @Test
    void testDisableForeignKeyChecks() {
    }

    @Test
    void testEnableForeignKeyChecks() {
    }

    @Test
    void testIsCaseSensitive() {
    }

    @Test
    void testIsReservedWord() {
    }

    @Test
    void testCorrectObjectName() {
    }

    @Test
    void testIsFunction() {
    }

    @Test
    void testGetDataTypeMaxParameters() {
    }

    @Test
    void testGetDefaultSchema() {
    }

    @Test
    void testDataTypeIsNotModifiable() {
    }

    @Test
    void testGenerateDatabaseFunctionValue() {
    }

    @Test
    void testGetObjectQuotingStrategy() {
    }

    @Test
    void testSetObjectQuotingStrategy() {
    }

    @Test
    void testCreatesIndexesForForeignKeys() {
    }

    @Test
    void testGetOutputDefaultSchema() {
    }

    @Test
    void testSetOutputDefaultSchema() {
    }

    @Test
    void testIsDefaultSchema() {
    }

    @Test
    void testIsDefaultCatalog() {
    }

    @Test
    void testGetOutputDefaultCatalog() {
    }

    @Test
    void testSetOutputDefaultCatalog() {
    }

    @Test
    void testSupportsPrimaryKeyNames() {
    }

    @Test
    void testSupportsNotNullConstraintNames() {
    }

    @Test
    void testSupportsBatchUpdates() {
    }

    @Test
    void testRequiresExplicitNullForColumns() {
    }

    @Test
    void testGetSystemSchema() {
    }

    @Test
    void testEscapeDataTypeName() {
    }

    @Test
    void testUnescapeDataTypeName() {
    }

    @Test
    void testUnescapeDataTypeString() {
    }

    @Test
    void testValidate() {
    }

    @Test
    void getPriority() {
    }

    @Test
    void getDefaultDriver() {
    }

    @Test
    void testRequiresUsername() {
    }

    @Test
    void testRequiresPassword() {
    }

    @Test
    void testGetAutoCommitMode() {
    }

    @Test
    void testSupportsDDLInTransaction() {
    }

    @Test
    void testGetDatabaseProductName() {
    }

    @Test
    void getDefaultDatabaseProductName() {
    }

    @SneakyThrows
    @Test
    void isCorrectDatabaseImplementation() {
        CosmosLiquibaseDatabase database = new CosmosLiquibaseDatabase();
        assertThat(database.isCorrectDatabaseImplementation(null)).isFalse();
        assertThat(database.isCorrectDatabaseImplementation(new JdbcConnection())).isFalse();
        assertThat(database.isCorrectDatabaseImplementation(new CosmosConnection())).isTrue();
    }

    @SneakyThrows
    @Test
    void findCorrectDatabaseImplementation() {
        final CosmosConnection connection = new CosmosConnection();
        final CosmosLiquibaseDatabase database =
                (CosmosLiquibaseDatabase) DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
        assertThat(database).isNotNull();
        assertThat(database.getConnection()).isEqualTo(connection);
    }

    @Test
    void testToString1() {
    }

    @Test
    void testGetConnection() {
    }

    @Test
    void testSetDatabaseChangeLogLockTableName() {
    }

    @Test
    void testSetLiquibaseCatalogName() {
    }

    @Test
    void testSetConnection() {
    }
}