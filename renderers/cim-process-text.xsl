<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
  <xsl:template match="/CIM/MESSAGE/SIMPLERSP/IMETHODRESPONSE/IRETURNVALUE">
PSID<xsl:text>&#x9;</xsl:text>Pri<xsl:text>&#x9;</xsl:text>Name
----<xsl:text>&#x9;</xsl:text>---<xsl:text>&#x9;</xsl:text>----
    <xsl:for-each xml:space="preserve" select="VALUE.OBJECTWITHPATH/INSTANCE">
<xsl:if test="PROPERTY[@NAME='Name'] != ''"><xsl:value-of select="normalize-space(PROPERTY[@NAME='ProcessSessionID'])"/>
<xsl:text>&#x9;</xsl:text><xsl:value-of select="normalize-space(PROPERTY[@NAME='Priority'])"/><xsl:text>&#x9;</xsl:text><xsl:value-of select="normalize-space(PROPERTY[@NAME='Name'])"/><xsl:text>&#xa;</xsl:text></xsl:if></xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
