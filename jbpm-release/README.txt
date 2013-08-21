PURPOSE
    - provide modules for a JBoss EAP6* environment that can support jbpm core engine
    - the target environment for these modules is the "jbpm-engine" openshift cartridge found here:
        https://github.com/jbride/openshift-origin-cartridge-jbpm-engine


Design considerations
    content
        - the output of this build should be a zip file that includes the small sub-set of droolsjbpm libraries and dependencies to support a jbpm process engine

    build artifact size
        - the size of the build artifact (bpms.deployer.zip) from this prooject is currently 14 MB.
        - keeping the build artifact size small will subsequently keep the jbpm-engine "downloadable" cartridge small as well
        - this becomes beneficial each time the cartridge is downloaded from the internet

    runtime considerations
        - the jbpm-engine cartridge should be capable of running comfortably in an Openshift small gear
        - subsequently, the modules provided by this project should ensure that only the bare minimum classes are loaded into PermGen
        - using these modules, the PermGen size set for the jbpm-engine cartridge is currently set at ~ 150 MB ... which is acceptable.


    1)  jboss-eap-6.1.0.zip downloaded and unzipped at $JBOSS_HOME
            or
    2)  

usage:
  1) from this directory, execute:   mvn clean install -Dmaven.test.skip=true
    - upon completion, should see the following: jboss-eap6.1/modules/target/bpms.deployer.zip

  2) deployment into runtime environment
    a)  if local EAP 6.1 environment
        - unzip jboss-eap6.1/modules/target/bpms.deployer.zip -d $JBOSS_HOME
    b)  if "jbpm-engine" cartridge
            cp jboss-eap6.1/modules/target/bpms.deployer.zip /path/to/jbpm-engine/cartridge/versions/6.0/modules/


notes:
  1)  new 'bpms' module layer will be placed here:   $JBOSS_HOME/modules/system/layers/bpms
  2)  $JBOSS_HOME/modules/layers.conf will be modified such that it now registers the new 'bpms' module layer
  3)  applications leveraging these new 'bpms' modules will want to define the following dependency in their jboss-deployment-structure.xml :

        ....
        <dependencies>

            <module name="org.jbpm" export="true" meta-inf="true" />

        </dependencies>


