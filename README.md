# Liquibase Cosmos DB Core (SQL) API Extension

[![Build Status]()]()

## Table of contents

1. [Introduction](#introduction)
1. [Implemented Changes](#implemented-changes)
1. [Getting Started](#getting-started)
1. [Running tests](#running-tests)
1. [Integration](#integration)
1. [Contributing](#contributing)
1. [License](#license)

<a name="introduction"></a>
## Introduction
<p>
Azure Cosmos DB is a globally distributed multi-model database. 
One of the supported APIs is the SQL API, which provides a JSON document model with SQL querying and JavaScript procedural logic.
</p>
<p>

This is a Liquibase extension for [Azure Cosmos DB Core (SQL) API](https://docs.microsoft.com/en-us/rest/api/cosmos-db/)  support. 
It uses internally [Azure Cosmos DB Java SDK v4 for Core (SQL)](https://docs.microsoft.com/en-us/azure/cosmos-db/sql-api-sdk-java-v4).
It is an alternative to existing evolution tools.  

</p>

<p>

In order to call specific Java SDK specific methods, 
Liquibase turned to be the most feasible tool to extend as it allows defining change sets to fit driver methods arguments.
Parameters are usually sent as Attributes ans JSON Payloads.
While implementing we tried to stay as close as possible to respective Rest Endpoint API Payload structure and Java SDK method naming. 

</p>


<a name="implemented-changes"></a>
## Implemented Changes:

The available changes implemented are:

* createDatabase [REST](https://docs.microsoft.com/en-us/rest/api/cosmos-db/create-a-database) [SDK]()
<p>
This is implicit change not available for use and it is called when Connection is initialised. 
A database (with provided DatabaseName in JSON connection string) is crested if not exists. 
All subsequent changes are applied to the created DB.
</p>

* createContainer [REST](https://docs.microsoft.com/en-us/rest/api/cosmos-db/create-a-collection) [SDK](https://docs.microsoft.com/en-us/java/api/com.azure.cosmos.cosmosdatabase.createcontainer?view=azure-java-stable)
<p> 
Creates a Cosmos container while passing additional request properties.
There is a possibility to pass throughput properties either manual as a number or auto as a json.
There is a flag to skip if exists and do not fail.
If no properties are specified then a ``/null`` partition key path is the default one.
</p>

* replaceContainer [REST](https://docs.microsoft.com/en-us/rest/api/cosmos-db/replace-a-collection) [SDK](https://docs.microsoft.com/en-us/java/api/com.azure.cosmos.cosmoscontainer.replace?view=azure-java-stable)
<p>
Replaces the container properties by container id.
There is a possibility to pass throughput properties either manual as a number or auto as a json.
If only properties specified no throughput properties will be amended and viceversa.
</p>

* deleteContainer [REST](https://docs.microsoft.com/en-us/rest/api/cosmos-db/delete-a-collection) [SDK](https://docs.microsoft.com/en-us/java/api/com.azure.cosmos.cosmoscontainer.delete?view=azure-java-stable)
<p>
Deletes the Cosmos container by id.
There is a flag to skip if missing and do not fail.
</p>

* createStoredProcedure [REST](https://docs.microsoft.com/en-us/rest/api/cosmos-db/create-a-stored-procedure) [SDK](https://docs.microsoft.com/en-us/java/api/com.azure.cosmos.cosmosscripts.createstoredprocedure?view=azure-java-stable)
<p> 
The Create Stored Procedure operation creates a new stored procedure in a collection.
There is a flag to replace if exists and do not fail.
</p>

* deleteStoredProcedure [REST](https://docs.microsoft.com/en-us/rest/api/cosmos-db/delete-a-stored-procedure) [SDK](https://docs.microsoft.com/en-us/java/api/com.azure.cosmos.cosmosstoredprocedure.delete?view=azure-java-stable)
<p> 
The Delete Stored Procedure operation deletes a stored procedure in a collection.
There is a flag to skip if missing and do not fail.
</p>

* createItem [REST](https://docs.microsoft.com/en-us/rest/api/cosmos-db/create-a-document) [SDK](https://docs.microsoft.com/en-us/java/api/com.azure.cosmos.cosmoscontainer.createitem?view=azure-java-stable)
<p>
Creates a new item(Document) synchronously in the container specified name.
</p>

* upsertItem [REST](https://docs.microsoft.com/en-us/rest/api/cosmos-db/replace-a-document) [SDK](https://docs.microsoft.com/en-us/java/api/com.azure.cosmos.cosmoscontainer.upsertitem?view=azure-java-stable)
<p>
Upserts an Cosmos item in the current container. If item is not found by id it will be created otherwise will be updated.
</p>

* updateEachItem [REST](https://docs.microsoft.com/en-us/rest/api/cosmos-db/query-documents) [SDK](https://docs.microsoft.com/en-us/java/api/com.azure.cosmos.cosmoscontainer.upsertitem?view=azure-java-stable)
<p>
This is a custom operation not provided by API that updates each item in the query. 
Was implemented by exposing Query Option document and the fields to be merged(updated).
Firstly the items are selected by query parameters, 
then for each item the fields are added from the update document, 
lastly the obtained item is upserted back.
</p>

* deleteEachItem [REST](https://docs.microsoft.com/en-us/rest/api/cosmos-db/delete-a-document) [SDK](https://docs.microsoft.com/en-us/java/api/com.azure.cosmos.cosmoscontainer.deleteitem?view=azure-java-stable)
<p>
This is a custom operation not provided by API that deletes each item in the query. 
Was implemented by exposing Query Option document. 
Recommended to make projections in the query as deletion is done by id field, thus is not require to fetch all fields.
Firstly the items are selected by query parameters, 
then for each item a delete by id is performed. 
</p>

<a name="getting-started"></a>
## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 

### Prerequisites


#### Install [Azure Cosmos DB Emulator](https://docs.microsoft.com/en-us/azure/cosmos-db/local-emulator?tabs=cli%2Cssl-netstd21)
<p>
If is required to test locally or install in On-Prem server, one can use the Emulator which is quite limited however enough for some initial testing.
</p>
<p>
Otherwise can be fallowed the [Java Application Quickstart Prerequisites](https://docs.microsoft.com/en-us/azure/cosmos-db/create-sql-api-java?tabs=sync)
</p>

#### Import keys to Trust Store
<p>
You need to export the emulator certificate to successfully use the emulator endpoint from languages and runtime environments that do not integrate with the Windows Certificate Store.

[Export the Azure Cosmos DB TLS/SSL certificate](https://docs.microsoft.com/en-us/azure/cosmos-db/local-emulator-export-ssl-certificates)
```shell script
sudo $JAVA_HOME/bin/keytool -delete -alias cosmos_emulator
# import the cert
sudo $JAVA_HOME/bin/keytool -importcert -keystore cacerts.jks -alias cosmos_emulator -file cosmos_emulator.cer
```
</p>

<p>
After certificate is imported should be passed as system parameters:

```shell script
mvn -Djavax.net.ssl.trustStore="<path_to_certs>\cacerts.jks" -Djavax.net.ssl.trustStorePassword=changeit
```
### Installing

* Clone the project
* [Run tests](#running-tests)

<a name="running-tests"></a>
## Running tests

### Adjust connection string
 
Connection url can be adjusted here: [`db.connection.uri`](./src/test/resources/application-test.properties)

Connection String Format has to be prefixed with ```cosmosdb://```
Can understand two formats:

#### Json Format after the prefix

```json
    {
      "accountEndpoint" : "https://[host]:[port]",
      "accountKey" : "[Account Key]", 
      "databaseName" : "[Database Name]"
    }
```
so a json url looks like:
```url
cosmosdb://{"accountEndpoint" : "https://localhost:8080", 
            "accountKey" : "C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==", 
            "databaseName" : "testdb1"}
```

#### Mongo URL like after the prefix 

```url
cosmosdb://[host]:[accountKey]@[host]:[port]/[databaseName]?[Query Parameters]
```
so a mongo like url looks like:
```url
cosmosdb://localhost:C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==@localhost:8080/testdb1
```

<p>
Connection string for now doesn't conform to any standard was just too convenient to parse from a JSON.
Field names are case-sensitive and kept as upper camel case as in Cosmos documentation.
Both formats accept other properties either as Json fields and respectively query parameters for future flexibility
(the only meaningful for nou is ```ssl=true/false```)
</p>

### Run integration tests

Integration tests can be run by enabling `run-its` profile 

```shell script
mvn clean install -Prun-its
```

<a name="integration"></a>
## Integration

### Add dependency: 

```xml
        <dependency>
            <groupId>com.mastercard.mcob</groupId>
            <artifactId>liquibase-cosmosdb</artifactId>
            <version>4.0.1-SNAPSHOT</version>
        </dependency>
```
### Java call:
You can initialise Liquibase and run from java in your application: 
See examples in [Integration Tests](/src/test/java/liquibase/ext/cosmosdb/CosmosLiquibaseIT.java) 

### Run using maven plugin

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.liquibase</groupId>
                <artifactId>liquibase-maven-plugin</artifactId>
                <version>4.0.0</version>
                <configuration>
                    <changeLogFile>[path_to_scripts]\scripts\changelog.main.xml</changeLogFile>
                    <url>cosmosdb://{"AccountEndpoint" : "https://ech-0a9d975b:8081, "AccountKey" : "C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==", "DatabaseName" : "testdb1"}</url>
                    <!-- TODO: required to be false as if true database.getConnection() throws NPE-->
                    <promptOnNonLocalDatabase>false</promptOnNonLocalDatabase>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>com.mastercard.mcob</groupId>
                        <artifactId>liquibase-cosmosdb</artifactId>
                        <version>4.0.1-SNAPSHOT</version>
                    </dependency>

                    <dependency>
                        <groupId>org.liquibase</groupId>
                        <artifactId>liquibase-core</artifactId>
                        <version>4.0.0</version>
                    </dependency>

                </dependencies>
            </plugin>
        </plugins>
    </build>

```

See [Demo Application](/src/test/liquibase-cosmosdb-example)
Working/tested plugin features: 
* liquibase:update - evolve
* liquibase:dropAll - cleanup database the created db will not be dropped
* liquibase:history - shows applied changesets
* liquibase:status - shows the pending changesets and invalid checksums
* liquibase:listLocks - lists the list of acquired locks
* liquibase:releaseLocks - releases the existing acquired locks

<a name="contributing"></a>
## Contributing

Please read [CONTRIBUTING.md](./CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

<a name="license"></a>
## License

This project is licensed under the Apache License Version 2.0 - see the [LICENSE.md](LICENSE.md) file for details



