# WorkItemHandler Archetype

Archetype used to build new jBPM Workitem Handlers.

This Archetype allows you to easily start building a new jBPM Workitem Handler.
I creates your WorkItem Handler Maven project that includes the base Handler class and 
test. It also makes sure your Maven project already includes the core dependencies 
for your Workitem Handler.

How to run it
--------------------
1. build the jbpm-workitems module (mvn clean install)
2. Change to directory of your choice where you want to build the 
base workitem handler from this archetype.
3. Create your new workitem handler from the archetype with:
```
mvn archetype:generate 
   -DarchetypeGroupId=org.jbpm 
   -DarchetypeArtifactId=jbpm-workitems-archetype 
   -DarchetypeVersion=8.0.0-SNAPSHOT
   -DgroupId=org.jbpm.demo.workitems 
   -DartifactId=myworkitem 
   -DclassPrefix=MyWorkItem
   -DarchetypeCatalog=local
```
or use this one-liner

```
mvn archetype:generate -DarchetypeGroupId=org.jbpm -DarchetypeArtifactId=jbpm-workitems-archetype -DarchetypeVersion=8.0.0-SNAPSHOT -DgroupId=org.jbpm.demo.workitems -DartifactId=myworkitem -DclassPrefix=MyWorkItem -DarchetypeCatalog=local
```
4. Change the prompted values during the generation as needed (or leave the defaults)
5. Compile and test your generated workitem handler with 
```
mvn clean install
```