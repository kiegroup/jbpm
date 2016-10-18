<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:drools="http://www.jboss.org/drools"
  xmlns:jpdl="urn:jbpm.org:jpdl-3.2" xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">

  <!-- Import the pieces of jPDL we need. -->
  <xsl:import href="event-bpmn.xsl" />
  <xsl:import href="transition-bpmn.xsl" />
  <xsl:import href="action-bpmn.xsl" />


  <xsl:template match="jpdl:super-state">
  
  	<!-- process internal nodes like normal but with super-state naming passed in as a parameter. -->
  	<xsl:apply-templates>
  		<xsl:with-param name="superstate" select="@name"/>
    </xsl:apply-templates>
  	
    
  </xsl:template>
</xsl:stylesheet>
