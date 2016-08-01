jBPM in container tests
=====================

**This test suite tests the jBPM engine inside various containers, namely WildFly 10, EAP 7, Tomcat 8,
Oracle WebLogic 12 and IBM WebSphere 8.5.5, using Maven Cargo and Arquillian.** Tests are focused on various aspects of jBPM engine 
such as transactions, EJB APIs, tasks, REST and WebServices integration.

This module consists of three submodules:
* jbpm-container-integration-deps - Various dependencies for Arquillian archives generated using ShrinkWrap
* jbpm-container-test-suite - Test suite itself
* shrinkwrap-war-profiles - Maven profiles for ShrinkWrap, they group various jbpm-container-integration-deps dependencies together

Tests are run very easily using the command

```mvn clean install -Dcontainer.profile=<container-profile> <container-specific-params>```

where `<container-profile>` is simply a particular container. Another container-specific parameters may also be configured (see the table below).
WildFly10, EAP 7 and Tomcat 8 do not have to be pre-installed, they will be downloaded automatically (in case of EAP 7, download URL has to be provided).
Oracle WebLogic 12 and IBM WebSphere 8.5.5 have to be pre-installed and the installation path has to be provided using a Maven property `weblogic.home` or `websphere.home` respectively.

The following table lists all currently supported combinations of parameters:

| Container to run    | \<container-profile\> | \<container-specific params\>             |
| -----------------   | --------------------- | ----------------------------------------- |
|     WildFly10       | wildfly10             | *none*                                    |
|     EAP 7           | eap7                  | eap7.download.url<sup>1</sup>             |
|     EAP 6.4         | ea64                  | eap64.download.url<sup>1</sup>            |
|     Tomcat 7        | tomcat7               | *none*                                    |
|     Tomcat 8        | tomcat8               | *none*                                    |
| Oracle WebLogic 12  | oracle-wls-12         | weblogic.home                             |
| IBM WebSphere 8.5.5 | was85x                | websphere.home, env.WAS85_HOME<sup>2</sup>|

<sup>1</sup> If you plan to frequently change versions of EAP and your zip files have the same name (e.g. server.zip), 
make sure you clean cached download directory before each run. The default location is ```${java.io.tmpdir}/cargo/installs```

<sup>2</sup> Special property for Arquillian adapter for WebSphere, the value is the same as websphere.home

## Database configuration
By default, the tests are run with the H2 database. If you want to change the database, simply override **Datasource properties** in the **jbpm-container-test-suite/pom.xml** file.

## WebSphere and Arquillian adapter
Currently, Arquillian adapter for WebSphere is not publicly available in Central Maven Repository. Instead, you have to build your own adapter and add it as a dependency to the profile **was85x** in the **jbpm-container-test-suite/pom.xml** file. This adapter's source code is available on [GitHub](https://github.com/arquillian/arquillian-container-was/tree/master/was-remote-8.5) as well.

## Running a single test
If you want to run a single test, just specify it via an additional parameter ```-Dit.test=<test-name>``` and add ```-DfailIfNoTests=false```. The latter has to be provided because of
maven failsafe plugin which will fail by default if no tests are run on other modules.