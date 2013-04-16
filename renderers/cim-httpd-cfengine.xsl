<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <!--
  <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
  -->
  <xsl:template match="/CIM/MESSAGE/SIMPLERSP/IMETHODRESPONSE/IRETURNVALUE">
    <xsl:for-each xml:space="preserve" select="VALUE.NAMEDINSTANCE/INSTANCE">
        Document root: <xsl:value-of select="normalize-space(PROPERTY[@NAME='DocumentRoot'])"/>
        <xsl:value-of select="normalize-space(PROPERTY[@NAME='PortNumber'])"/>
        <xsl:value-of select="normalize-space(PROPERTY[@NAME='InstanceID'])"/>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
