<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
    <xsl:template match="/CIM/MESSAGE/SIMPLERSP/IMETHODRESPONSE/IRETURNVALUE">
body common control
{
  bundlesequence  => { "set_listener_port" };
  inputs => { "/var/cfengine/masterfiles/cfengine_stdlib.cf" };
}

bundle agent set_listener_port
{
files:
  "/etc/apache2/listen.conf"
  edit_line => replace_line_end("^Listen", "<xsl:value-of select="normalize-space(VALUE.NAMEDINSTANCE/INSTANCE/PROPERTY[@NAME='PortNumber'])"/>");
}
  </xsl:template>
</xsl:stylesheet>
