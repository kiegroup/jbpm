<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:jpdl="urn:jbpm.org:jpdl-3.2"
  xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">

  <!-- TODO: future work, to try and come up with a way to make the         -->
  <!--       id's of variables unique when the name of the var's remain the -->
  <!--       same throughout the process. See insuranceMainProcess for an   -->
  <!--       example of having to make them unique in the process.          -->
 
  <!-- Import the pieces of jPDL we need. -->
  <xsl:import href="sub-process-bpmn.xsl" />

  <xsl:template match="jpdl:process-state">
    <callActivity>
      <xsl:attribute name="name">
        <xsl:value-of select="@name" />
	  </xsl:attribute>
      <xsl:attribute name="id">
        <xsl:value-of select="translate(@name,' ', '_')" />
      </xsl:attribute>
      
      <xsl:if test="jpdl:description">
        <xsl:apply-templates select="jpdl:description" />
      </xsl:if>
      
      <xsl:attribute name="calledElement">
       	<xsl:apply-templates select="jpdl:sub-process" />
      </xsl:attribute>
      <ioSpecification>
   		<xsl:for-each select="jpdl:variable">
		  	<dataInput>
				<xsl:attribute name="id">
					<xsl:value-of select="translate(@name,' ','_')" />
					<xsl:text>_</xsl:text>
		   			<xsl:text>Input</xsl:text>
		  		</xsl:attribute>
		        <xsl:attribute name="name">
					<xsl:value-of select="@mapped-name" /> 
		  		</xsl:attribute>
	        </dataInput>
		</xsl:for-each>
		<xsl:for-each select="jpdl:variable">
			<dataOutput>
		        <xsl:attribute name="id">
					<xsl:value-of select="translate(@name,' ','_')" />
				   	<xsl:text>_</xsl:text>
		   			<xsl:text>Output</xsl:text>
				</xsl:attribute>
		        <xsl:attribute name="name">
					<xsl:value-of select="@mapped-name"/>
				</xsl:attribute>
	        </dataOutput>
		</xsl:for-each>
	    <inputSet>
			<xsl:for-each select="jpdl:variable">
    	      	<dataInputRefs>
					<xsl:value-of select="translate(@name,' ','_')" />
            		<xsl:text>_</xsl:text>
            		<xsl:text>Input</xsl:text>
          		</dataInputRefs>
			</xsl:for-each>
       	</inputSet>
	    <outputSet>
			<xsl:for-each select="jpdl:variable">
    			<dataOutputRefs>
					<xsl:value-of select="translate(@name,' ','_')" />
            		<xsl:text>_</xsl:text>
            		<xsl:text>Output</xsl:text>
          		</dataOutputRefs>
			</xsl:for-each>
       	</outputSet>
      </ioSpecification>
	  <xsl:for-each select="jpdl:variable">
      	<dataInputAssociation>
        	<sourceRef>
			  <xsl:value-of select="translate(@name,' ','_')" />
          		<xsl:text>_</xsl:text>
          		<xsl:text>Input</xsl:text> 
        	</sourceRef>
        	<targetRef>
				<xsl:value-of select="translate(@name,' ','_')" />
          		<xsl:text>_</xsl:text>
          		<xsl:text>Output</xsl:text>
        	</targetRef>
      	</dataInputAssociation>
      </xsl:for-each> 	
	  <xsl:for-each select="jpdl:variable">
	      <dataOutputAssociation>
    	    <sourceRef>
			  <xsl:value-of select="translate(@name,' ','_')" />
 	          <xsl:text>_</xsl:text>
   		      <xsl:text>Output</xsl:text>
        	</sourceRef>
        	<targetRef>
				<xsl:value-of select="translate(@name,' ','_')" />
 	          <xsl:text>_</xsl:text>
   		      <xsl:text>Input</xsl:text>
        	</targetRef>
      	 </dataOutputAssociation>
      </xsl:for-each> 	
	</callActivity>

    <xsl:apply-templates select="jpdl:transition" />
  </xsl:template>

  <!-- Removes description element from the transformation. -->
  <xsl:template match="jpdl:description" />

</xsl:stylesheet>
