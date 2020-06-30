# Running JUnit tests with JPA persistence

Out of the box [H2 in-memory database](https://www.h2database.com/) is used for those test cases where a database is required to be in place. 
So no any additional or further configuration is needed here.

However, there might be some other cases where you might want to execute these tests against a different database, such as [Oracle, DB2, etc.](#bulb-note-database-certification-matrix-can-be-found-here)
In order to address this, you must provide these tests with some additional configuration (Maven parameters) when testing them via Maven.

These Maven properties are mainly used in both `datasource.properties` and `META-INF/persistence.xml` files, usually located under the `test/filtered-resources` folder in the project.

## Maven database properties

| Property | Description | Example |
| :---         | :---         | :---         |
| `maven.datasource.classname`   | Datasource classname for the particular database datasource | _com.ibm.db2.jcc.DB2XADataSource_ |
| `maven.jdbc.driver.class` | JDBC driver class for the specific database | _com.ibm.db2.jcc.DB2Driver_ |
| `maven.jdbc.username` | Database username | _john_ |
| `maven.jdbc.password` | Database password | _password123_ |
| `maven.jdbc.url` | Database URL to connect to | _jdbc:db2://localhost:50000/DBALLO1_ |
| `maven.jdbc.db.server` | Server name where the database is running | _localhost_ |
| `maven.jdbc.db.port` | Database port | _50000_ |
| `maven.jdbc.db.name` | Database name | _DBALLO1_ |
| `maven.hibernate.dialect` | Dialect class to be used by JPA provider (i.e. Hibernate) | _org.hibernate.dialect.DB2Dialect_ |
| `maven.jdbc.schema` | Default database schema name | _DBALLO1_ |


### Maven use examples

* DB2 database   
```
$ mvn test -DskipTests=false \
    -Dmaven.jdbc.password=password123 \
    -Dmaven.jdbc.url=jdbc:db2://localhost:50000/DBALLO1 \
    -Dmaven.jdbc.db.port=50000 \
    -Dmaven.hibernate.dialect=org.hibernate.dialect.DB2Dialect \
    -Dmaven.jdbc.db.name=DBALLO1 \
    -Dmaven.datasource.classname=com.ibm.db2.jcc.DB2XADataSource \
    -Dmaven.jdbc.driver.class=com.ibm.db2.jcc.DB2Driver \
    -Dmaven.jdbc.schema=DBALLO1 \
    -Dmaven.jdbc.username=john \
    -Dmaven.jdbc.db.server=localhost
```
* Oracle database   
```
$ mvn test -DskipTests=false \
    -Dmaven.jdbc.password=password123 \
    -Dmaven.jdbc.url=jdbc:oracle:thin:@//localhost:1521/ORCLCDB \
    -Dmaven.hibernate.dialect=org.hibernate.dialect.Oracle12cDialect \
    -Dmaven.jdbc.db.name=ORCLPDB1 \
    -Dmaven.datasource.classname=oracle.jdbc.xa.client.OracleXADataSource \
    -Dmaven.jdbc.driver.class=oracle.jdbc.OracleDriver \
    -Dmaven.jdbc.schema=john \
    -Dmaven.jdbc.username=john
```

##### :bulb: Note: Database certification matrix can be found [here](https://access.redhat.com/articles/3405381).
