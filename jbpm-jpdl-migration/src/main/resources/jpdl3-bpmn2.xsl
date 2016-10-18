<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:jpdl="urn:jbpm.org:jpdl-3.2"
  xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">

  <!-- Import the pieces of jPDL we need. -->
  <xsl:import href="start-state-bpmn.xsl" />
  <xsl:import href="process-state-bpmn.xsl" />
  <xsl:import href="task-node-bpmn.xsl" />
  <xsl:import href="node-bpmn.xsl" />
  <xsl:import href="state-bpmn.xsl" />
  <xsl:import href="decision-bpmn.xsl" />
  <xsl:import href="forkjoin-bpmn.xsl" />
  <xsl:import href="transition-bpmn.xsl" />
  <xsl:import href="super-state-bpmn.xsl" />
  <xsl:import href="end-state-bpmn.xsl" />
  <xsl:import href="swimlane-bpmn.xsl" />

  <xsl:output method="xml" indent="yes" />

  <xsl:variable name="allowedSymbols" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'" />
  
  <xsl:template match="/">
    <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
      xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
      xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:drools="http://www.jboss.org/drools" targetNamespace="http://www.jbpm.org/">
      <xsl:attribute name="id">
   		<xsl:value-of select="translate(jpdl:process-definition/@name,' ','_')" />
      </xsl:attribute>
      <xsl:attribute name="name">
        <xsl:value-of select="translate(jpdl:process-definition/@name,' ','_')" />
      </xsl:attribute>
      <xsl:apply-templates select="jpdl:process-definition" />
    </definitions>
  </xsl:template>

  <xsl:template match="jpdl:process-definition">
    <process>
      <xsl:attribute name="id">
	  	<xsl:value-of select="translate(@name,' ','_')" /><xsl:text>_Process</xsl:text>
      </xsl:attribute>
      <xsl:attribute name="name">
	  	<xsl:value-of select="@name" />
      </xsl:attribute>

      <xsl:if test="jpdl:description">
        <xsl:apply-templates select="jpdl:description" />
      </xsl:if>

      <xsl:apply-templates />
    </process>
  </xsl:template>

  <!-- Removes description element from the transformation. -->
  <xsl:template match="jpdl:description" />

  <!-- Strip the white space from the result. -->
  <xsl:template match="text()">
    <xsl:value-of select="normalize-space()" />
  </xsl:template>

</xsl:stylesheet>
