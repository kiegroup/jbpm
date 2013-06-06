pre-reqs:
    1)  jboss-eap-6.1.0.zip downloaded and unzipped at $JBOSS_HOME
   

usage:
  1) from this directory, execute:   mvn clean install -Dmaven.test.skip=true
    - upon completion, should see the following: jboss-eap6.1/modules/target/bpms.deployer.zip

  2) unzip jboss-eap6.1/modules/target/bpms.deployer.zip -d $JBOSS_HOME


notes:
  1)  new 'bpms' module layer will be placed here:   $JBOSS_HOME/modules/system/layers/bpms
  2)  $JBOSS_HOME/modules/layers.conf will be modified such that it now registers the new 'bpms' module layer
  3)  applications leveraging these new 'bpms' modules will want to define the following dependency in their jboss-deployment-structure.xml :

        ....
        <dependencies>

            <module name="org.jbpm" export="true" />

        </dependencies>


