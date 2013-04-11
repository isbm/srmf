<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
  <xsl:template match="/">
class host_<xsl:value-of select="normalize-space(/CIM/MESSAGE/SIMPLERSP/IMETHODRESPONSE/IRETURNVALUE/VALUE.OBJECTWITHPATH/INSTANCEPATH/NAMESPACEPATH/HOST)"/>_pkg_replica {
<xsl:for-each xml:space="preserve" select="/CIM/MESSAGE/SIMPLERSP/IMETHODRESPONSE/IRETURNVALUE/VALUE.OBJECTWITHPATH/INSTANCE">
        package { "<xsl:value-of select="normalize-space(PROPERTY[@NAME='Name'])"/>": ensure =<xsl:text>&gt;</xsl:text> "installed" }</xsl:for-each>
}
  </xsl:template>
</xsl:stylesheet>
