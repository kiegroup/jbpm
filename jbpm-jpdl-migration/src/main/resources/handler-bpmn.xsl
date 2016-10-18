<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:jpdl="urn:jbpm.org:jpdl-3.2"
  xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">

  <!-- Processing handler elements in documentation element. -->
  <xsl:template match="jpdl:handler">
    <documentation>
      <xsl:text>This decision makes use of the following handler: </xsl:text>
      <xsl:value-of select="@class" />
      <xsl:text>.</xsl:text>
    </documentation>
  </xsl:template>

  <!-- Processing handler elements to return the class name. -->
  <xsl:template match="jpdl:handler" mode="classname">
    <xsl:value-of select="@class" />
  </xsl:template>

</xsl:stylesheet>
