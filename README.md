Service Management Framework
============================

Service Management Framework is intended to describe, recast and [re]deploy services in the data center.
Please note, the project is in the early bloody stage and everything changes faster than tornado! It also does not mean that everything here makes perfect sense... :)

Requirements
------------

Currently it is in early prototype stage and only requires Java RTE installed on your machine. To compile sources, you will need Java SDK and Ant 1.8.0+.

Configuration
-------------

### SRMF main config

This is the main config to describe the entire service (one box or many):

1. Make sure CIM broker is running on your target machine (usually localhost).
2. Edit srmf.conf authentication, see the example.
3. Set your manifest path other than /tmp :)
4. Configure your host auth. Syntax is: "hostname/qualifier". Qualifiers are: "proto" for protocol; "port" for port; "user" is for user name to authenticate against CIM broker and "password" is the password there.

### SRMF object providers for entity description

This config is for gathering system description (srmf-objects.xml). At the moment there is no mechanism used for making sure some providers not hanging etc, hence it exists. In a fugure it might be only optional or disappear at all.

Objects can be combined into one as well, using WBEM Query Language. More: http://www.wbemsolutions.com/tutorials/DMTF/wbem-cql.html

### SRMF object mapping for manifest export and deploying

Deployment config (srmf-map.xml) exists for mapping rendering XSL stylesheets to particular providers. Map refers to the object providers above and can select which of the objects "in charge" to go "outside" of the description. In other words, not all objects needs to be exported to particular destinations, like plain text or some CMS etc.

Usage Examples
--------------

Show classes under the default namespace:

    $ java -jar srmf.jar --hostname=foo.bar.com --show-classes

Describe the entire class (or classes, if comma separated). The description returns just message XML:

    $ java -jar srmf.jar --hostname=foo.bar.com --describe=MY_Object,MY_OtherObject

Display supported configuration management destinations:

    $ java -jar srmf.jar --hostname=foo.bar.com --available-cms

Typical output should be:

    ID          Title
    --------------------------------------------------------
    cfengine-3	CFengine v.3 configuration management system
    text	    ASCII plain text

Export the system description for deployment through the CFengine v3 could be done this way:

    $ java -jar srmf.jar --hostname=foo.bar.com --export=cfengine-3

Note: export is in the very early stage and it needs more attention for separation between scenarios and files for various CMS. At the moment working ony "text" and it give just a bare output to the STDOUT.

To export the configuration to your USB stick and bring to another machine for 3rd party processing, simply use "snapshot":

    $ java -jar srmf.jar --hostname=foo.bar.com --snapshot

Typical output should be something like this:
    
    > Written /tmp/srmf/manifest/cim_process.lmx
    > Written /tmp/srmf/manifest/cim_computersystem.lmx

Please note that ".lmx" extension is for compressed XML.

To manually process this data on completely another system, you can use XSL stylesheets from the ".srmf.manifest.renderers" in the srmf.conf and run with any XSLT processor. For example, typically you can use "xsltproc":

    $ zcat /tmp/srmf/manifest/cim_process.lmx | xsltproc renderers/cim-process-text.xsl -

The command above would be the same part as of exporting to "text" (see few examples above), except it would run only across one object. Please note, that depending on your XPath processor, output might be a bit different.
