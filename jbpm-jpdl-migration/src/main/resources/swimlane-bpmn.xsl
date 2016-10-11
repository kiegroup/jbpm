<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:jpdl="urn:jbpm.org:jpdl-3.2"
	xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">

	<xsl:output method="xml" indent="yes" />

	<!-- The next 2 templates create the wrapper <laneSet> element, which wraps the <lane> jpdl does not have such a wrapper element, so we need 
		to perform some tricks to create a wrapper :-) -->
	<xsl:template match="jpdl:swimlane">
		<!-- Check that the previous sibling of our current node is not a swimlane. -->
		<xsl:if test="not(preceding-sibling::*[1][self::jpdl:swimlane])">
			<!-- Create the wrapper <laneSet> element -->
			<laneSet>
				<!-- The 'build-lane' template actually builds the 'lane' element. -->
				<xsl:apply-templates mode="build-lane" select="current()" />
				<!-- Select the immediate following sibling only if it is named "jpdl:swimlane". Template 'mode' is 'in-list', which basically makes sure 
					that we apply the template below instead of this one. -->
				<xsl:apply-templates mode="in-list" select="following-sibling::*[1][self::jpdl:swimlane]" />
			</laneSet>
		</xsl:if>
	</xsl:template>

	<xsl:template match="jpdl:swimlane" mode="in-list">
		<!-- The 'build-lane' template actually builds the 'lane' element. -->
		<xsl:apply-templates mode="build-lane" select="current()" />
		<!-- Select the immediate following sibling only if it is named "jpdl:swimlane". Template 'mode' is 'in-list', which causes this template to 
			be called recursively. -->
		<xsl:apply-templates mode="in-list" select="following-sibling::*[1][self::jpdl:swimlane]" />
	</xsl:template>

	<!-- Builds the actual <lane> element from the jpdl <swimlane> element. -->
	<xsl:template match="jpdl:swimlane" mode="build-lane">
		<lane>
			<xsl:attribute name="name">
	  			<xsl:value-of select="@name" />
      		</xsl:attribute>
			<!-- Now we need to find all the <task-node> nodes, which have 'swimlane' attribute with value equal to the 'name' of this <swimlane>. From 
				these nodes we'll build the <flowNodeRef> elements. -->
			<xsl:apply-templates mode="build-flowNodeRef" select="/jpdl:process-definition/jpdl:task-node[jpdl:task/@swimlane=current()/@name]" />
		</lane>
	</xsl:template>

	<xsl:template match="jpdl:task-node" mode="build-flowNodeRef">
		<flowNodeRef>
			<xsl:value-of select="@name" />
		</flowNodeRef>
	</xsl:template>
</xsl:stylesheet>
