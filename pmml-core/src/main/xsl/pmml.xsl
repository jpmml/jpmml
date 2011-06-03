<?xml version="1.0" ?>
<!--
Copyright (c) 2009 University of Tartu
-->
<xsl:stylesheet version="1.0" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="xsd:element[@ref='Extension'][position() &gt; 1 and position() = last()]">
		<xsl:comment> &lt;xs:element ref=&quot;Extension&quot; minOccurs=&quot;0&quot; maxOccurs=&quot;unbounded&quot;/&gt; </xsl:comment>
	</xsl:template>
</xsl:stylesheet>