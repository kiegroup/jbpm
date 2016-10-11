<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:drools="http://www.jboss.org/drools"
  xmlns:jpdl="urn:jbpm.org:jpdl-3.2" xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">

  <!-- Import the pieces of jPDL we need. -->
  <xsl:import href="transition-bpmn.xsl" />
  <xsl:import href="event-bpmn.xsl" />
  <xsl:import href="action-bpmn.xsl" />

  <xsl:output method="xml" />

  <xsl:template match="jpdl:start-state">

    <!-- In case of an event, we will create a startEvent and use -->
    <!-- a Java Node from project to process the event action class. -->
    <xsl:choose>

      <xsl:when test="(jpdl:event) or (jpdl:action)">
        <startEvent>
          <xsl:attribute name="name">
       		<xsl:value-of select="@name" />
          </xsl:attribute>
          <xsl:attribute name="id">
          	<xsl:value-of select="translate(@name,' ','_')" />
          </xsl:attribute>

          <xsl:if test="jpdl:description">
            <xsl:apply-templates select="jpdl:description" />
          </xsl:if>
        </startEvent>

        <!-- Inserting sequence flow from startEvent to Java Node. -->
        <sequenceFlow>
          <xsl:attribute name="id">
		  	<xsl:text>flow_</xsl:text>
		  	<xsl:value-of select="translate(@name,' ','_')" />
		  </xsl:attribute>
          <xsl:attribute name="sourceRef">
		    <xsl:value-of select="translate(@name,' ','_')" />
		  </xsl:attribute>
          <xsl:attribute name="targetRef">
			<xsl:text>javanode_</xsl:text>
			<xsl:value-of select="translate(@name,' ','_')" />
		  </xsl:attribute>
        </sequenceFlow>

        <task>
          <xsl:attribute name="id">
			<xsl:text>javanode_</xsl:text>
			<xsl:value-of select="translate(@name,' ','_')" />
		  </xsl:attribute>
          <xsl:attribute name="name">
			<xsl:text>added to execute: </xsl:text>
			<xsl:value-of select="translate(@name,' ','_')" />
		  </xsl:attribute>
          <xsl:attribute name="drools:taskName">
			<xsl:text>JavaNode</xsl:text>
		  </xsl:attribute>

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
                    <xsl:apply-templates select="jpdl:event" mode="classname" />
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

        <xsl:apply-templates select="jpdl:transition" mode="start-event-javanode" />
      </xsl:when>

      <xsl:otherwise>
        <startEvent>
          <xsl:attribute name="name">
            <xsl:value-of select="@name" />
          </xsl:attribute>
          <xsl:attribute name="id">
            <xsl:value-of select="translate(@name,' ','_')" />
          </xsl:attribute>

          <xsl:if test="jpdl:description">
            <xsl:apply-templates select="jpdl:description" />
          </xsl:if>
        </startEvent>

        <xsl:apply-templates select="jpdl:transition" />
      </xsl:otherwise>

    </xsl:choose>

  </xsl:template>

  <!-- Removes description element from the transformation. -->
  <xsl:template match="jpdl:description" />

</xsl:stylesheet>
