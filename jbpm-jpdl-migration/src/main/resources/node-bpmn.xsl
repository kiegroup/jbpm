<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:drools="http://www.jboss.org/drools"
  xmlns:jpdl="urn:jbpm.org:jpdl-3.2" xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">

  <!-- Import the pieces of jPDL we need. -->
  <xsl:import href="event-bpmn.xsl" />
  <xsl:import href="transition-bpmn.xsl" />
  <xsl:import href="action-bpmn.xsl" />

  <xsl:template match="jpdl:node">
  
	<xsl:param name="superstate" />

	<!-- In case of an event, we will use Java Nodes from project -->
    <!-- to process the handler classes. -->
    <xsl:choose>

    <xsl:when test="(jpdl:event) and (count(jpdl:event) > 1)">
    
    	<!--  task - sequence - scriptTask (node) - sequence - task -->
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
        
        <!-- Inserting sequence flow from eventNodeEnter to scriptTask. -->
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
			<xsl:text>nodetask_</xsl:text>
			<xsl:value-of select="translate(@name,' ','_')" />
		  </xsl:attribute>
        </sequenceFlow>
                
        <scriptTask>
          <xsl:attribute name="name">
            <xsl:value-of select="@name" />
          </xsl:attribute>
          <xsl:attribute name="id">
            <xsl:text>nodetask_</xsl:text>
            <xsl:value-of select="translate(@name,' ','_')" />
          </xsl:attribute>

          <xsl:if test="jpdl:description">
            <xsl:apply-templates select="jpdl:description" />
          </xsl:if>

          <xsl:if test="jpdl:event">
            <script>
              // place holder for the following action handlers,
              // so you can migrate the code here:
              //
              <xsl:apply-templates select="jpdl:event" />
            </script>
          </xsl:if>
        </scriptTask>

         <!-- Inserting sequence flow from scriptTask to eventNodeLeave. -->
        <sequenceFlow>
          <xsl:attribute name="id">
		  	<xsl:text>flow_</xsl:text>
		  	<xsl:value-of select="translate(@name,' ','_')" />
		  	<xsl:value-of select="position()+2" />
		  </xsl:attribute>
          <xsl:attribute name="sourceRef">
          	<xsl:text>nodetask_</xsl:text>
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
				<xsl:text>_methodInput</xsl:text>
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
                <xsl:text>_methodInput</xsl:text>
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
              <xsl:text>_methodInput</xsl:text>
            </targetRef>
            <assignment>
              <from>
                <xsl:text>execute</xsl:text>
              </from>
              <to>
                <xsl:value-of select="@name" />
                <xsl:text>_methodInput</xsl:text>
              </to>
            </assignment>
          </dataInputAssociation>
        </task>
        
        <xsl:apply-templates select="jpdl:transition" mode="node-leave-event"/>
      </xsl:when>

	  <!--  Just a single event, either node enter or node leave. -->
	  <xsl:when test="(jpdl:event) and (count(jpdl:event) = 1)">

		<xsl:if test="(jpdl:event)/@type='node-enter'">
		 
		 	<!--  task - sequence - scriptTask -->
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
					<xsl:text>_classInput</xsl:text>
				  </xsl:attribute>
	              <xsl:attribute name="name">
					<xsl:text>class</xsl:text>
				  </xsl:attribute>
	            </dataInput>
	            <dataInput>
	              <xsl:attribute name="id">
					<xsl:value-of select="translate(@name,' ','_')" />
					<xsl:text>_methodInput</xsl:text>
				  </xsl:attribute>
	              <xsl:attribute name="name">
					<xsl:text>method</xsl:text>
				  </xsl:attribute>
	            </dataInput>
	            <inputSet>
	              <dataInputRefs>
	                <xsl:value-of select="translate(@name,' ','_')" />
	                <xsl:text>_classInput</xsl:text>
	              </dataInputRefs>
	              <dataInputRefs>
	                <xsl:value-of select="translate(@name,' ','_')" />
	                <xsl:text>_methodInput</xsl:text>
	              </dataInputRefs>
	            </inputSet>
	            <outputSet />
	          </ioSpecification>
	          <dataInputAssociation>
	            <targetRef>
	              <xsl:value-of select="translate(@name,' ','_')" />
	              <xsl:text>_classInput</xsl:text>
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
	                <xsl:text>_classInput</xsl:text>
	              </to>
	            </assignment>
	          </dataInputAssociation>
	          <dataInputAssociation>
	            <targetRef>
	              <xsl:value-of select="translate(@name,' ','_')" />
	              <xsl:text>_methodInput</xsl:text>
	            </targetRef>
	            <assignment>
	              <from>
	                <xsl:text>execute</xsl:text>
	              </from>
	              <to>
	                <xsl:value-of select="@name" />
	                <xsl:text>_methodInput</xsl:text>
	              </to>
	            </assignment>
	          </dataInputAssociation>
	        </task>
	        
	        <!-- Inserting sequence flow from eventNodeEnter to scriptTask. -->
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
				<xsl:text>nodetask_</xsl:text>
				<xsl:value-of select="translate(@name,' ','_')" />
			  </xsl:attribute>
	        </sequenceFlow>
	                
	        <scriptTask>
	          <xsl:attribute name="name">
	            <xsl:value-of select="@name" />
	          </xsl:attribute>
	          <xsl:attribute name="id">
	            <xsl:text>nodetask_</xsl:text>
	            <xsl:value-of select="translate(@name,' ','_')" />
	          </xsl:attribute>
	
	          <xsl:if test="jpdl:description">
	            <xsl:apply-templates select="jpdl:description" />
	          </xsl:if>
	
	          <xsl:if test="jpdl:event">
	            <script>
	              // place holder for the following action handlers,
	              // so you can migrate the code here:
	              //
	              <xsl:apply-templates select="jpdl:event" />
	            </script>
	          </xsl:if>
	        </scriptTask>
			 	        
			<xsl:apply-templates select="jpdl:transition" mode="leave-scripttask"/>
		</xsl:if>
		
		<xsl:if test="(jpdl:event)/@type='node-leave'">

		 	<!--  scriptTask - sequence - task -->
		 	<scriptTask>
	          <xsl:attribute name="name">
	            <xsl:value-of select="@name" />
	          </xsl:attribute>
	          <xsl:attribute name="id">
	            <xsl:value-of select="translate(@name,' ','_')" />
	          </xsl:attribute>
	
	          <xsl:if test="jpdl:description">
	            <xsl:apply-templates select="jpdl:description" />
	          </xsl:if>
	
	          <xsl:if test="jpdl:event">
	            <script>
	              // place holder for the following action handlers,
	              // so you can migrate the code here:
	              //
	              <xsl:apply-templates select="jpdl:event" />
	            </script>
	          </xsl:if>
	        </scriptTask>
	        
	        <!-- Inserting sequence flow from scriptTask to eventNodeLeave. -->
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
					<xsl:text>_classInput</xsl:text>
				  </xsl:attribute>
	              <xsl:attribute name="name">
					<xsl:text>class</xsl:text>
				  </xsl:attribute>
	            </dataInput>
	            <dataInput>
	              <xsl:attribute name="id">
					<xsl:value-of select="translate(@name,' ','_')" />
					<xsl:text>_methodInput</xsl:text>
				  </xsl:attribute>
	              <xsl:attribute name="name">
					<xsl:text>method</xsl:text>
				  </xsl:attribute>
	            </dataInput>
	            <inputSet>
	              <dataInputRefs>
	                <xsl:value-of select="translate(@name,' ','_')" />
	                <xsl:text>_classInput</xsl:text>
	              </dataInputRefs>
	              <dataInputRefs>
	                <xsl:value-of select="translate(@name,' ','_')" />
	                <xsl:text>_methodInput</xsl:text>
	              </dataInputRefs>
	            </inputSet>
	            <outputSet />
	          </ioSpecification>
	          <dataInputAssociation>
	            <targetRef>
	              <xsl:value-of select="translate(@name,' ','_')" />
	              <xsl:text>_classInput</xsl:text>
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
	                <xsl:text>_classInput</xsl:text>
	              </to>
	            </assignment>
	          </dataInputAssociation>
	          <dataInputAssociation>
	            <targetRef>
	              <xsl:value-of select="translate(@name,' ','_')" />
	              <xsl:text>_methodInput</xsl:text>
	            </targetRef>
	            <assignment>
	              <from>
	                <xsl:text>execute</xsl:text>
	              </from>
	              <to>
	                <xsl:value-of select="@name" />
	                <xsl:text>_methodInput</xsl:text>
	              </to>
	            </assignment>
	          </dataInputAssociation>
	        </task>
	        
	        <xsl:apply-templates select="jpdl:transition" mode="node-leave-event"/>
		</xsl:if>
      </xsl:when>
      
      <xsl:otherwise>
        <scriptTask>
          <xsl:attribute name="name">
          	<xsl:if test="string-length($superstate) > 0">
          		<xsl:value-of select="$superstate" />
				<xsl:text>_</xsl:text>
          	</xsl:if>
            <xsl:value-of select="@name" />
          </xsl:attribute>
          <xsl:attribute name="id">
          	<xsl:if test="string-length($superstate) > 0">
          		<xsl:value-of select="$superstate" />
				<xsl:text>_</xsl:text>
          	</xsl:if>
            <xsl:value-of select="translate(@name,' ','_')" />
          </xsl:attribute>

          <xsl:if test="jpdl:action">
            <script>
              // place holder for the following action handlers,
              // so you can migrate the code here:
              //          
              <xsl:apply-templates select="jpdl:action" />
            </script>
          </xsl:if>

          <xsl:if test="jpdl:description">
            <xsl:apply-templates select="jpdl:description" />
          </xsl:if>

          <xsl:if test="jpdl:event">
            <script>
              // place holder for the following action handlers,
              // so you can migrate the code here:
              //
              <xsl:apply-templates select="jpdl:event" />
            </script>
          </xsl:if>
 
          <xsl:if test="jpdl:transition/jpdl:action">
            <script>
              // place holder for the following action handlers,
              // so you can migrate the code here:
              //
              <xsl:value-of select="jpdl:transition/jpdl:action/@class" />
              
            </script>
          </xsl:if>
 
          <xsl:if test="jpdl:transition/jpdl:script">
            <script>
              // found the following script code, 
              // please evaluate:
              //
              
              <xsl:value-of select="jpdl:transition/jpdl:script" />
              
            </script>
          </xsl:if>
          
          
        </scriptTask>

        <xsl:apply-templates select="jpdl:transition">
        	<xsl:with-param name="superstate" select="$superstate" />
        </xsl:apply-templates>
      </xsl:otherwise>

    </xsl:choose>

    
  </xsl:template>

  <!-- Removes description element from the transformation. -->
  <xsl:template match="jpdl:description" />
</xsl:stylesheet>
