<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:jpdl="urn:jbpm.org:jpdl-3.2"
  xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">

  <!-- Sub-process element supplied the name of the process -->
  <!-- to be called. -->
  <xsl:template match="jpdl:sub-process">
    <xsl:value-of select="@name" />
  </xsl:template>

</xsl:stylesheet>
