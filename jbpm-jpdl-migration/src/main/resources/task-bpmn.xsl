<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:drools="http://www.jboss.org/drools"
  xmlns:jpdl="urn:jbpm.org:jpdl-3.2" xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">

  <!-- Import the pieces of jPDL we need. -->
  <xsl:import href="assignment-bpmn.xsl" />

  <xsl:template match="jpdl:task" mode="task">
    <xsl:value-of select="@name" />

    <xsl:if test="jpdl:description">
      <xsl:apply-templates select="jpdl:description" />
    </xsl:if>
  </xsl:template>



  <xsl:template match="jpdl:task" mode="assignment">
    <xsl:if test="jpdl:description">
      <xsl:apply-templates select="jpdl:description" />
    </xsl:if>

    <xsl:if test="jpdl:assignment/@actor-id">
      <potentialOwner>
        <resourceAssignmentExpression>
          <formalExpression>
            <xsl:apply-templates select="jpdl:assignment" />
          </formalExpression>
        </resourceAssignmentExpression>
      </potentialOwner>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
