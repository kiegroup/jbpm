<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:drools="http://www.jboss.org/drools"
  xmlns:jpdl="urn:jbpm.org:jpdl-3.2" xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">

  <!-- Import the pieces of jPDL we need. -->
  <xsl:import href="event-bpmn.xsl" />
  <xsl:import href="transition-bpmn.xsl" />
  <xsl:import href="action-bpmn.xsl" />
  <xsl:import href="task-bpmn.xsl" />
  <xsl:import href="assignment-bpmn.xsl" />

  <xsl:template match="jpdl:task-node">

    <!-- In case of an event, we will use Java Nodes from project -->
    <!-- to process the handler classes. -->
    <xsl:choose>

    <xsl:when test="(jpdl:event) and (count(jpdl:event) > 1)">
    
    	<!--  task - sequence - userTask - sequence - task -->
        <task>
          <xsl:attribute name="id">
			<xsl:value-of select="translate(@name,' ','_')" />
	      </xsl:attribute>
          <xsl:attribute name="name">
          	<xsl:text>Expanded to execute: </xsl:text>
			<xsl:value-of select="@name" />
          	<xsl:text> enter</xsl:text>
          </xsl:attribute>
          <xsl:attribute name="drools:taskName">
			<xsl:text>JavaNode</xsl:text>
		  </xsl:attribute>

          <xsl:if test="jpdl:description">
            <xsl:apply-templates select="jpdl:description" />
          </xsl:if>

          <ioSpecification>
            <dataInput>
              <xsl:attribute name="id">
				<xsl:value-of select="translate(@name,' ','_')" />
				<xsl:text>_classInput_</xsl:text>
				<xsl:value-of select="position()" />
			  </xsl:attribute>
              <xsl:attribute name="name">
				<xsl:text>class</xsl:text>
			  </xsl:attribute>
            </dataInput>
            <dataInput>
              <xsl:attribute name="id">
				<xsl:value-of select="translate(@name,' ','_')" />
				<xsl:text>_methodInput_</xsl:text>
				<xsl:value-of select="position()" />
			  </xsl:attribute>
              <xsl:attribute name="name">
				<xsl:text>method</xsl:text>
			  </xsl:attribute>
            </dataInput>
            <inputSet>
              <dataInputRefs>
                <xsl:value-of select="translate(@name,' ','_')" />
                <xsl:text>_classInput_</xsl:text>
                <xsl:value-of select="position()" />
              </dataInputRefs>
              <dataInputRefs>
                <xsl:value-of select="translate(@name,' ','_')" />
                <xsl:text>_methodInput_</xsl:text>
                <xsl:value-of select="position()" />
              </dataInputRefs>
            </inputSet>
            <outputSet />
          </ioSpecification>
          <dataInputAssociation>
            <targetRef>
              <xsl:value-of select="translate(@name,' ','_')" />
              <xsl:text>_classInput_</xsl:text>
              <xsl:value-of select="position()" />
            </targetRef>
            <assignment>
              <from>
                <xsl:choose>
                  <xsl:when test="jpdl:event">
                    <xsl:apply-templates select="jpdl:event" mode="enter" />
                  </xsl:when>

                  <xsl:when test="jpdl:action">
                    <xsl:apply-templates select="jpdl:action" />
                  </xsl:when>
                </xsl:choose>
              </from>
              <to>
                <xsl:value-of select="@name" />
                <xsl:text>_classInput_</xsl:text>
                <xsl:value-of select="position()" />
              </to>
            </assignment>
          </dataInputAssociation>
          <dataInputAssociation>
            <targetRef>
              <xsl:value-of select="translate(@name,' ','_')" />
              <xsl:text>_methodInput_</xsl:text>
              <xsl:value-of select="position()" />
            </targetRef>
            <assignment>
              <from>
                <xsl:text>execute</xsl:text>
              </from>
              <to>
                <xsl:value-of select="@name" />
                <xsl:text>_methodInput_</xsl:text>
                <xsl:value-of select="position()" />
              </to>
            </assignment>
          </dataInputAssociation>
        </task>

        <!-- Inserting sequence flow from eventNodeEnter to userTask. -->
        <sequenceFlow>
          <xsl:attribute name="id">
		  	<xsl:text>flow_</xsl:text>
		  	<xsl:value-of select="translate(@name,' ','_')" />
		  	<xsl:value-of select="position()" />
		  </xsl:attribute>
          <xsl:attribute name="sourceRef">
		    <xsl:value-of select="translate(@name,' ','_')" />
		  </xsl:attribute>
          <xsl:attribute name="targetRef">
			<xsl:text>usertask_</xsl:text>
			<xsl:value-of select="translate(@name,' ','_')" />
		  </xsl:attribute>
        </sequenceFlow>
        
        <!-- Inserting userTask node. -->
        <userTask>
          <xsl:attribute name="id">
			<xsl:text>usertask_</xsl:text>
            <xsl:value-of select="translate(@name,' ','_')" />
		  </xsl:attribute>
          <xsl:attribute name="name">
          	<xsl:text>Expanded to provide: </xsl:text>
			<xsl:value-of select="@name" />
		  </xsl:attribute>
          <xsl:attribute name="drools:taskName">
			<xsl:text>JavaNode</xsl:text>
		  </xsl:attribute>

          <xsl:if test="jpdl:description">
            <xsl:apply-templates select="jpdl:description" />
          </xsl:if>

          <ioSpecification>
            <dataInput>
              <xsl:attribute name="id">
				<xsl:value-of select="translate(@name,' ','_')" />
				<xsl:text>_classInput_</xsl:text>
				<xsl:value-of select="position()+1" />
			  </xsl:attribute>
              <xsl:attribute name="name">
				<xsl:text>class</xsl:text>
			  </xsl:attribute>
            </dataInput>
            <dataInput>
              <xsl:attribute name="id">
				<xsl:value-of select="translate(@name,' ','_')" />
				<xsl:text>_methodInput_</xsl:text>
				<xsl:value-of select="position()+1" />
			  </xsl:attribute>
              <xsl:attribute name="name">
				<xsl:text>method</xsl:text>
			  </xsl:attribute>
            </dataInput>
            <inputSet>
              <dataInputRefs>
                <xsl:value-of select="translate(@name,' ','_')" />
                <xsl:text>_classInput_</xsl:text>
                <xsl:value-of select="position()+1" />
              </dataInputRefs>
              <dataInputRefs>
                <xsl:value-of select="translate(@name,' ','_')" />
                <xsl:text>_methodInput_</xsl:text>
                <xsl:value-of select="position()+1" />
              </dataInputRefs>
            </inputSet>
            <outputSet />
          </ioSpecification>
          <dataInputAssociation>
            <targetRef>
              <xsl:value-of select="translate(@name,' ','_')" />
              <xsl:text>_classInput_</xsl:text>
              <xsl:value-of select="position()+1" />
            </targetRef>
            <assignment>
              <from>
                <xsl:choose>
                  <xsl:when test="jpdl:action">
                    <xsl:apply-templates select="jpdl:action" />
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:text>execute</xsl:text>
                  </xsl:otherwise>
                </xsl:choose>
              </from>
              <to>
                <xsl:value-of select="@name" />
                <xsl:text>_classInput_</xsl:text>
                <xsl:value-of select="position()+1" />
              </to>
            </assignment>
          </dataInputAssociation>
          <dataInputAssociation>
            <targetRef>
              <xsl:value-of select="translate(@name,' ','_')" />
              <xsl:text>_methodInput_</xsl:text>
              <xsl:value-of select="position()+1" />
            </targetRef>
            <assignment>
              <from>
                <xsl:text>execute</xsl:text>
              </from>
              <to>
                <xsl:value-of select="@name" />
                <xsl:text>_methodInput_</xsl:text>
                <xsl:value-of select="position()+1" />
              </to>
            </assignment>
          </dataInputAssociation>
        </userTask>
        
        <!-- Inserting sequence flow from userTask to eventNodeLeave task. -->
        <sequenceFlow>
          <xsl:attribute name="id">
		  	<xsl:text>flow_</xsl:text>
		  	<xsl:value-of select="translate(@name,' ','_')" />
		  	<xsl:value-of select="position()+1" />
		  </xsl:attribute>
          <xsl:attribute name="sourceRef">
            <xsl:text>usertask_</xsl:text>
		    <xsl:value-of select="translate(@name,' ','_')" />
		  </xsl:attribute>
          <xsl:attribute name="targetRef">
			<xsl:text>javanode_leavenode_</xsl:text>
			<xsl:value-of select="translate(@name,' ','_')" />
		  </xsl:attribute>
        </sequenceFlow>
                
        <task>
          <xsl:attribute name="id">
			<xsl:text>javanode_leavenode_</xsl:text>
			<xsl:value-of select="translate(@name,' ','_')" />
		  </xsl:attribute>
          <xsl:attribute name="name">
          	<xsl:text>Expanded to execute: </xsl:text>
			<xsl:value-of select="translate(@name,' ','_')" />
          	<xsl:text> leave</xsl:text>
		  </xsl:attribute>
          <xsl:attribute name="drools:taskName">
			<xsl:text>JavaNode</xsl:text>
		  </xsl:attribute>
		  
          <xsl:if test="jpdl:description">
            <xsl:apply-templates select="jpdl:description" />
          </xsl:if>

          <ioSpecification>
            <dataInput>
              <xsl:attribute name="id">
				<xsl:value-of select="translate(@name,' ','_')" />
				<xsl:text>_classInput_</xsl:text>
				<xsl:value-of select="position()+2" />
			  </xsl:attribute>
              <xsl:attribute name="name">
				<xsl:text>class</xsl:text>
			  </xsl:attribute>
            </dataInput>
            <dataInput>
              <xsl:attribute name="id">
				<xsl:value-of select="translate(@name,' ','_')" />
				<xsl:text>_methodInput_</xsl:text>
				<xsl:value-of select="position()+2" />
			  </xsl:attribute>
              <xsl:attribute name="name">
				<xsl:text>method</xsl:text>
			  </xsl:attribute>
            </dataInput>
            <inputSet>
              <dataInputRefs>
                <xsl:value-of select="translate(@name,' ','_')" />
                <xsl:text>_classInput_</xsl:text>
                <xsl:value-of select="position()+2" />
              </dataInputRefs>
              <dataInputRefs>
                <xsl:value-of select="translate(@name,' ','_')" />
                <xsl:text>_methodInput_</xsl:text>
                <xsl:value-of select="position()+2" />
              </dataInputRefs>
            </inputSet>
            <outputSet />
          </ioSpecification>
          <dataInputAssociation>
            <targetRef>
              <xsl:value-of select="translate(@name,' ','_')" />
              <xsl:text>_classInput_</xsl:text>
              <xsl:value-of select="position()+2" />
            </targetRef>
            <assignment>
              <from>
                <xsl:choose>
                  <xsl:when test="jpdl:event">
                    <xsl:apply-templates select="jpdl:event" mode="leave" />
                  </xsl:when>

                  <xsl:when test="jpdl:action">
                    <xsl:apply-templates select="jpdl:action" />
                  </xsl:when>
                </xsl:choose>
              </from>
              <to>
                <xsl:value-of select="@name" />
                <xsl:text>_classInput_</xsl:text>
                <xsl:value-of select="position()+2" />
              </to>
            </assignment>
          </dataInputAssociation>
          <dataInputAssociation>
            <targetRef>
              <xsl:value-of select="translate(@name,' ','_')" />
              <xsl:text>_methodInput_</xsl:text>
              <xsl:value-of select="position()+2" />
            </targetRef>
            <assignment>
              <from>
                <xsl:text>execute</xsl:text>
              </from>
              <to>
                <xsl:value-of select="@name" />
                <xsl:text>_methodInput_</xsl:text>
                <xsl:value-of select="position()+2" />
              </to>
            </assignment>
          </dataInputAssociation>
        </task>
        
        <xsl:apply-templates select="jpdl:transition" mode="javanode-leavenode-humantask" />
      </xsl:when>
      
      <xsl:when test="(jpdl:event) and (count(jpdl:event) = 1)">
    
    	<xsl:if test="(jpdl:event)/@type='node-enter'">
		 
			<!--  task - sequence - userTask  -->
	       	<task>
	          <xsl:attribute name="id">
				<xsl:value-of select="translate(@name,' ','_')" />
		      </xsl:attribute>
	          <xsl:attribute name="name">
	          	<xsl:text>Expanded to execute: </xsl:text>
				<xsl:value-of select="@name" />
	          	<xsl:text> enter</xsl:text>
	          </xsl:attribute>
	          <xsl:attribute name="drools:taskName">
				<xsl:text>JavaNode</xsl:text>
			  </xsl:attribute>
	
	          <xsl:if test="jpdl:description">
	            <xsl:apply-templates select="jpdl:description" />
	          </xsl:if>
	
	          <ioSpecification>
	            <dataInput>
	              <xsl:attribute name="id">
					<xsl:value-of select="translate(@name,' ','_')" />
					<xsl:text>_classInput_</xsl:text>
					<xsl:value-of select="position()" />
				  </xsl:attribute>
	              <xsl:attribute name="name">
					<xsl:text>class</xsl:text>
				  </xsl:attribute>
	            </dataInput>
	            <dataInput>
	              <xsl:attribute name="id">
					<xsl:value-of select="translate(@name,' ','_')" />
					<xsl:text>_methodInput_</xsl:text>
					<xsl:value-of select="position()" />
				  </xsl:attribute>
	              <xsl:attribute name="name">
					<xsl:text>method</xsl:text>
				  </xsl:attribute>
	            </dataInput>
	            <inputSet>
	              <dataInputRefs>
	                <xsl:value-of select="translate(@name,' ','_')" />
	                <xsl:text>_classInput_</xsl:text>
	                <xsl:value-of select="position()" />
	              </dataInputRefs>
	              <dataInputRefs>
	                <xsl:value-of select="translate(@name,' ','_')" />
	                <xsl:text>_methodInput_</xsl:text>
	                <xsl:value-of select="position()" />
	              </dataInputRefs>
	            </inputSet>
	            <outputSet />
	          </ioSpecification>
	          <dataInputAssociation>
	            <targetRef>
	              <xsl:value-of select="translate(@name,' ','_')" />
	              <xsl:text>_classInput_</xsl:text>
	              <xsl:value-of select="position()" />
	            </targetRef>
	            <assignment>
	              <from>
	                <xsl:choose>
	                  <xsl:when test="jpdl:event">
	                    <xsl:apply-templates select="jpdl:event" mode="enter" />
	                  </xsl:when>
	
	                  <xsl:when test="jpdl:action">
	                    <xsl:apply-templates select="jpdl:action" />
	                  </xsl:when>
	                </xsl:choose>
	              </from>
	              <to>
	                <xsl:value-of select="@name" />
	                <xsl:text>_classInput_</xsl:text>
	                <xsl:value-of select="position()" />
	              </to>
	            </assignment>
	          </dataInputAssociation>
	          <dataInputAssociation>
	            <targetRef>
	              <xsl:value-of select="translate(@name,' ','_')" />
	              <xsl:text>_methodInput_</xsl:text>
	              <xsl:value-of select="position()" />
	            </targetRef>
	            <assignment>
	              <from>
	                <xsl:text>execute</xsl:text>
	              </from>
	              <to>
	                <xsl:value-of select="@name" />
	                <xsl:text>_methodInput_</xsl:text>
	                <xsl:value-of select="position()" />
	              </to>
	            </assignment>
	          </dataInputAssociation>
	        </task>
	
	        <!-- Inserting sequence flow from eventNodeEnter to userTask. -->
	        <sequenceFlow>
	          <xsl:attribute name="id">
			  	<xsl:text>flow_</xsl:text>
			  	<xsl:value-of select="translate(@name,' ','_')" />
			  	<xsl:value-of select="position()" />
			  </xsl:attribute>
	          <xsl:attribute name="sourceRef">
			    <xsl:value-of select="translate(@name,' ','_')" />
			  </xsl:attribute>
	          <xsl:attribute name="targetRef">
				<xsl:text>usertask_</xsl:text>
				<xsl:value-of select="translate(@name,' ','_')" />
			  </xsl:attribute>
	        </sequenceFlow>
	        
	        <!-- Inserting userTask node. -->
	        <userTask>
	          <xsl:attribute name="id">
				<xsl:text>usertask_</xsl:text>
	            <xsl:value-of select="translate(@name,' ','_')" />
			  </xsl:attribute>
	          <xsl:attribute name="name">
	          	<xsl:text>Expanded to provide: </xsl:text>
				<xsl:value-of select="@name" />
			  </xsl:attribute>
	          <xsl:attribute name="drools:taskName">
				<xsl:text>JavaNode</xsl:text>
			  </xsl:attribute>
	
	          <xsl:if test="jpdl:description">
	            <xsl:apply-templates select="jpdl:description" />
	          </xsl:if>
	
	          <ioSpecification>
	            <dataInput>
	              <xsl:attribute name="id">
					<xsl:value-of select="translate(@name,' ','_')" />
					<xsl:text>_classInput_</xsl:text>
					<xsl:value-of select="position()+1" />
				  </xsl:attribute>
	              <xsl:attribute name="name">
					<xsl:text>class</xsl:text>
				  </xsl:attribute>
	            </dataInput>
	            <dataInput>
	              <xsl:attribute name="id">
					<xsl:value-of select="translate(@name,' ','_')" />
					<xsl:text>_methodInput_</xsl:text>
					<xsl:value-of select="position()+1" />
				  </xsl:attribute>
	              <xsl:attribute name="name">
					<xsl:text>method</xsl:text>
				  </xsl:attribute>
	            </dataInput>
	            <inputSet>
	              <dataInputRefs>
	                <xsl:value-of select="translate(@name,' ','_')" />
	                <xsl:text>_classInput_</xsl:text>
	                <xsl:value-of select="position()+1" />
	              </dataInputRefs>
	              <dataInputRefs>
	                <xsl:value-of select="translate(@name,' ','_')" />
	                <xsl:text>_methodInput_</xsl:text>
	                <xsl:value-of select="position()+1" />
	              </dataInputRefs>
	            </inputSet>
	            <outputSet />
	          </ioSpecification>
	          <dataInputAssociation>
	            <targetRef>
	              <xsl:value-of select="translate(@name,' ','_')" />
	              <xsl:text>_classInput_</xsl:text>
	              <xsl:value-of select="position()+1" />
	            </targetRef>
	            <assignment>
	              <from>
	                <xsl:choose>
	                  <xsl:when test="jpdl:action">
	                    <xsl:apply-templates select="jpdl:action" />
	                  </xsl:when>
	                  <xsl:otherwise>
	                    <xsl:text>execute</xsl:text>
	                  </xsl:otherwise>
	                </xsl:choose>
	              </from>
	              <to>
	                <xsl:value-of select="@name" />
	                <xsl:text>_classInput_</xsl:text>
	                <xsl:value-of select="position()+1" />
	              </to>
	            </assignment>
	          </dataInputAssociation>
	          <dataInputAssociation>
	            <targetRef>
	              <xsl:value-of select="translate(@name,' ','_')" />
	              <xsl:text>_methodInput_</xsl:text>
	              <xsl:value-of select="position()+1" />
	            </targetRef>
	            <assignment>
	              <from>
	                <xsl:text>execute</xsl:text>
	              </from>
	              <to>
	                <xsl:value-of select="@name" />
	                <xsl:text>_methodInput_</xsl:text>
	                <xsl:value-of select="position()+1" />
	              </to>
	            </assignment>
	          </dataInputAssociation>
	        </userTask>
	        
	        <xsl:apply-templates select="jpdl:transition" mode="node-leave-humantask" />
		</xsl:if>
		
		<xsl:if test="(jpdl:event)/@type='node-leave'">
		 
			<!--  userTask - sequence - task  -->
	        <userTask>
	          <xsl:attribute name="id">
	            <xsl:value-of select="translate(@name,' ','_')" />
			  </xsl:attribute>
	          <xsl:attribute name="name">
	          	<xsl:text>Expanded to provide: </xsl:text>
				<xsl:value-of select="@name" />
			  </xsl:attribute>
	          <xsl:attribute name="drools:taskName">
				<xsl:text>JavaNode</xsl:text>
			  </xsl:attribute>
	
	          <xsl:if test="jpdl:description">
	            <xsl:apply-templates select="jpdl:description" />
	          </xsl:if>
	
	          <ioSpecification>
	            <dataInput>
	              <xsl:attribute name="id">
					<xsl:value-of select="translate(@name,' ','_')" />
					<xsl:text>_classInput_</xsl:text>
					<xsl:value-of select="position()" />
				  </xsl:attribute>
	              <xsl:attribute name="name">
					<xsl:text>class</xsl:text>
				  </xsl:attribute>
	            </dataInput>
	            <dataInput>
	              <xsl:attribute name="id">
					<xsl:value-of select="translate(@name,' ','_')" />
					<xsl:text>_methodInput_</xsl:text>
					<xsl:value-of select="position()" />
				  </xsl:attribute>
	              <xsl:attribute name="name">
					<xsl:text>method</xsl:text>
				  </xsl:attribute>
	            </dataInput>
	            <inputSet>
	              <dataInputRefs>
	                <xsl:value-of select="translate(@name,' ','_')" />
	                <xsl:text>_classInput_</xsl:text>
	                <xsl:value-of select="position()" />
	              </dataInputRefs>
	              <dataInputRefs>
	                <xsl:value-of select="translate(@name,' ','_')" />
	                <xsl:text>_methodInput_</xsl:text>
	                <xsl:value-of select="position()" />
	              </dataInputRefs>
	            </inputSet>
	            <outputSet />
	          </ioSpecification>
	          <dataInputAssociation>
	            <targetRef>
	              <xsl:value-of select="translate(@name,' ','_')" />
	              <xsl:text>_classInput_</xsl:text>
	              <xsl:value-of select="position()" />
	            </targetRef>
	            <assignment>
	              <from>
	                <xsl:choose>
	                  <xsl:when test="jpdl:action">
	                    <xsl:apply-templates select="jpdl:action" />
	                  </xsl:when>
	                  <xsl:otherwise>
	                    <xsl:text>execute</xsl:text>
	                  </xsl:otherwise>
	                </xsl:choose>
	              </from>
	              <to>
	                <xsl:value-of select="@name" />
	                <xsl:text>_classInput_</xsl:text>
	                <xsl:value-of select="position()" />
	              </to>
	            </assignment>
	          </dataInputAssociation>
	          <dataInputAssociation>
	            <targetRef>
	              <xsl:value-of select="translate(@name,' ','_')" />
	              <xsl:text>_methodInput_</xsl:text>
	              <xsl:value-of select="position()" />
	            </targetRef>
	            <assignment>
	              <from>
	                <xsl:text>execute</xsl:text>
	              </from>
	              <to>
	                <xsl:value-of select="@name" />
	                <xsl:text>_methodInput_</xsl:text>
	                <xsl:value-of select="position()" />
	              </to>
	            </assignment>
	          </dataInputAssociation>
	        </userTask>
	        
	        <!-- Inserting sequence flow from userTask to eventNodeLeave task. -->
	        <sequenceFlow>
	          <xsl:attribute name="id">
			  	<xsl:text>flow_</xsl:text>
			  	<xsl:value-of select="translate(@name,' ','_')" />
			  	<xsl:value-of select="position()+1" />
			  </xsl:attribute>
	          <xsl:attribute name="sourceRef">
			    <xsl:value-of select="translate(@name,' ','_')" />
			  </xsl:attribute>
	          <xsl:attribute name="targetRef">
				<xsl:text>javanode_leavenode_</xsl:text>
				<xsl:value-of select="translate(@name,' ','_')" />
			  </xsl:attribute>
	        </sequenceFlow>
	                
	        <task>
	          <xsl:attribute name="id">
				<xsl:text>javanode_leavenode_</xsl:text>
				<xsl:value-of select="translate(@name,' ','_')" />
			  </xsl:attribute>
	          <xsl:attribute name="name">
	          	<xsl:text>Expanded to execute: </xsl:text>
				<xsl:value-of select="translate(@name,' ','_')" />
	          	<xsl:text> leave</xsl:text>
			  </xsl:attribute>
	          <xsl:attribute name="drools:taskName">
				<xsl:text>JavaNode</xsl:text>
			  </xsl:attribute>
			  
	          <xsl:if test="jpdl:description">
	            <xsl:apply-templates select="jpdl:description" />
	          </xsl:if>
	
	          <ioSpecification>
	            <dataInput>
	              <xsl:attribute name="id">
					<xsl:value-of select="translate(@name,' ','_')" />
					<xsl:text>_classInput_</xsl:text>
					<xsl:value-of select="position()+1" />
				  </xsl:attribute>
	              <xsl:attribute name="name">
					<xsl:text>class</xsl:text>
				  </xsl:attribute>
	            </dataInput>
	            <dataInput>
	              <xsl:attribute name="id">
					<xsl:value-of select="translate(@name,' ','_')" />
					<xsl:text>_methodInput_</xsl:text>
					<xsl:value-of select="position()+1" />
				  </xsl:attribute>
	              <xsl:attribute name="name">
					<xsl:text>method</xsl:text>
				  </xsl:attribute>
	            </dataInput>
	            <inputSet>
	              <dataInputRefs>
	                <xsl:value-of select="translate(@name,' ','_')" />
	                <xsl:text>_classInput_</xsl:text>
	                <xsl:value-of select="position()+1" />
	              </dataInputRefs>
	              <dataInputRefs>
	                <xsl:value-of select="translate(@name,' ','_')" />
	                <xsl:text>_methodInput_</xsl:text>
	                <xsl:value-of select="position()+1" />
	              </dataInputRefs>
	            </inputSet>
	            <outputSet />
	          </ioSpecification>
	          <dataInputAssociation>
	            <targetRef>
	              <xsl:value-of select="translate(@name,' ','_')" />
	              <xsl:text>_classInput_</xsl:text>
	              <xsl:value-of select="position()+1" />
	            </targetRef>
	            <assignment>
	              <from>
	                <xsl:choose>
	                  <xsl:when test="jpdl:event">
	                    <xsl:apply-templates select="jpdl:event" mode="leave" />
	                  </xsl:when>
	
	                  <xsl:when test="jpdl:action">
	                    <xsl:apply-templates select="jpdl:action" />
	                  </xsl:when>
	                </xsl:choose>
	              </from>
	              <to>
	                <xsl:value-of select="@name" />
	                <xsl:text>_classInput_</xsl:text>
	                <xsl:value-of select="position()+1" />
	              </to>
	            </assignment>
	          </dataInputAssociation>
	          <dataInputAssociation>
	            <targetRef>
	              <xsl:value-of select="translate(@name,' ','_')" />
	              <xsl:text>_methodInput_</xsl:text>
	              <xsl:value-of select="position()+1" />
	            </targetRef>
	            <assignment>
	              <from>
	                <xsl:text>execute</xsl:text>
	              </from>
	              <to>
	                <xsl:value-of select="@name" />
	                <xsl:text>_methodInput_</xsl:text>
	                <xsl:value-of select="position()+1" />
	              </to>
	            </assignment>
	          </dataInputAssociation>
	        </task>
        
        <xsl:apply-templates select="jpdl:transition" mode="javanode-leavenode-humantask" />
		</xsl:if>
		
		<xsl:if test="(jpdl:event)/@type='task-create'">
		<!--  todo: create this one, extract these event types to separate xsl sheets. -->
		</xsl:if>
	</xsl:when>	
	
	<xsl:otherwise>
	
        <userTask>
          <xsl:attribute name="id">
            <xsl:value-of select="translate(@name,' ','_')" />
		  </xsl:attribute>
          <xsl:attribute name="name">
			<xsl:value-of select="@name" />
		  </xsl:attribute>
          <xsl:attribute name="drools:taskName">
			<xsl:text>JavaNode</xsl:text>
		  </xsl:attribute>

          <xsl:if test="jpdl:description">
            <xsl:apply-templates select="jpdl:description" />
          </xsl:if>

          <ioSpecification>
            <dataInput>
              <xsl:attribute name="id">
				<xsl:value-of select="translate(@name,' ','_')" />
				<xsl:text>_classInput_</xsl:text>
				<xsl:value-of select="position()" />
			  </xsl:attribute>
              <xsl:attribute name="name">
				<xsl:text>class</xsl:text>
			  </xsl:attribute>
            </dataInput>
            <dataInput>
              <xsl:attribute name="id">
				<xsl:value-of select="translate(@name,' ','_')" />
				<xsl:text>_methodInput_</xsl:text>
				<xsl:value-of select="position()" />
			  </xsl:attribute>
              <xsl:attribute name="name">
				<xsl:text>method</xsl:text>
			  </xsl:attribute>
            </dataInput>
            <inputSet>
              <dataInputRefs>
                <xsl:value-of select="translate(@name,' ','_')" />
                <xsl:text>_classInput_</xsl:text>
                <xsl:value-of select="position()" />
              </dataInputRefs>
              <dataInputRefs>
                <xsl:value-of select="translate(@name,' ','_')" />
                <xsl:text>_methodInput_</xsl:text>
                <xsl:value-of select="position()" />
              </dataInputRefs>
            </inputSet>
            <outputSet />
          </ioSpecification>
          <dataInputAssociation>
            <targetRef>
              <xsl:value-of select="translate(@name,' ','_')" />
              <xsl:text>_classInput_</xsl:text>
              <xsl:value-of select="position()" />
            </targetRef>
            <assignment>
              <from>
                <xsl:choose>
                  <xsl:when test="jpdl:event">
                    <xsl:apply-templates select="jpdl:event" mode="classname" />
                  </xsl:when>

                  <xsl:when test="jpdl:action">
                    <xsl:apply-templates select="jpdl:action" />
                  </xsl:when>
                </xsl:choose>
              </from>
              <to>
                <xsl:value-of select="@name" />
                <xsl:text>_classInput_</xsl:text>
                <xsl:value-of select="position()" />
              </to>
            </assignment>
          </dataInputAssociation>
          <dataInputAssociation>
            <targetRef>
              <xsl:value-of select="translate(@name,' ','_')" />
              <xsl:text>_methodInput_</xsl:text>
              <xsl:value-of select="position()" />
            </targetRef>
            <assignment>
              <from>
                <xsl:text>execute</xsl:text>
              </from>
              <to>
                <xsl:value-of select="@name" />
                <xsl:text>_methodInput_</xsl:text>
                <xsl:value-of select="position()" />
              </to>
            </assignment>
          </dataInputAssociation>
        </userTask>
        
        <xsl:apply-templates select="jpdl:transition" />
       </xsl:otherwise>

    </xsl:choose>

  </xsl:template>

  <!-- Removes description element from the transformation. -->
  <xsl:template match="jpdl:description" />

</xsl:stylesheet>
