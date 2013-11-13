#
# Author: Bo Maryniuk <bo@suse.de>
#
# The BSD 3-Clause License
# Copyright (c) 2013, SUSE Linux Products GmbH
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met: 
# 
# * Redistributions of source code must retain the above copyright notice, this
#   list of conditions and the following disclaimer.
#
# * Redistributions in binary form must reproduce the above copyright notice, this
#   list of conditions and the following disclaimer in the documentation and/or
#   other materials provided with the distribution.
#
# * Neither the name of the SUSE Linux Products GmbH nor the names of its contributors may
#   be used to endorse or promote products derived from this software without
#   specific prior written permission.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


import pywbem
import locale
import socket

from xml.dom import minidom as dom
import os
from pywbem.cim_provider2 import CIMProvider2


class Contact:
    """
    Contact element.
    """
    def __init__(self):
        self.email = ""


class Owner:
    """
    Owner element.
    """
    def __init__(self):
        self.name = ""
        self.contact = Contact()
        self.memo = ""
        self.description = ""


class SUSE_SystemOwner(CIMProvider2):
    """Instrument the CIM class SUSE_SystemOwner 
    Return OS owner data.
    """
    SRMF_CMDB_MAP = "/etc/srmf/cmdb.map"
    _meta = None

    def get_system_locale(self):
        """
        Get current system locale.
        """
        lc = locale.getdefaultlocale()[0].split("_")
        if len(lc) != 2:
            lc = ["en", "US"]

        return lc


    def get_node_owner(self):
        """
        Get node owner meta information from the SRMF.CMDB map.
        """
        if (not self._meta and os.path.exists(self.SRMF_CMDB_MAP)):
            self._meta = dom.parse(self.SRMF_CMDB_MAP)

        owner = Owner()
        if self._meta:
            # Takes only first owner
            for owner_node in self._meta.getElementsByTagName("node")[0].getElementsByTagName("owner"):
                owner.name = self.get_dom_value(owner_node, "name")
                owner.contact.email = self.get_dom_value(owner_node, "email")
                owner.memo = self.get_dom_value(owner_node, "memo")
                owner.description = "ToDO"
                break

        return owner


    def get_dom_value(self, doc, tag):
        """
        Get element value.
           Examples:
               <a>value-here</a>
               <b><![CDATA[ another value here ]]></b>
        """
        node = doc.getElementsByTagName(tag)
        value = []
        for n in node:
            for cn in n.childNodes:
                v = None
                if cn.nodeType == dom.Document.CDATA_SECTION_NODE:
                    v = (cn.nodeValue + "").strip()
                elif cn.nodeType == dom.Document.TEXT_NODE:
                    v = (cn.nodeValue + "").strip()
                if v:
                    value.append(v)
        return '\n'.join(value)


    def __init__ (self, env):
        logger = env.get_logger()
        logger.log_debug('Initializing provider %s from %s' \
                % (self.__class__.__name__, __file__))


    def get_instance(self, env, model):
        """
        Return an instance.
        """
        
        logger = env.get_logger()
        logger.log_debug('Entering %s.get_instance()' \
                % self.__class__.__name__)
        
        lc = self.get_system_locale()
        owner = self.get_node_owner()

        # TODO fetch system resource matching the following keys:
        model['CreationClassName'] = "SUSE_SystemOwner"
        model['Name'] = "SUSE_SystemOwner"

        model['CommonName'] = 'Owner'
        model['Description'] = owner.memo
        model['LocalityName'] = lc[-1]
        model['Mail'] = "root@" + socket.gethostname()
        model['OwnerContact'] = owner.contact.email
        model['OwnerDescription'] = owner.description
        model['OwnerName'] = owner.name
        model['PreferredLanguage'] = lc[0]
        model['Surname'] = owner.name

        return model


    def enum_instances(self, env, model, keys_only):
        """
        Enumerate instances.
        """

        logger = env.get_logger()
        logger.log_debug('Entering %s.enum_instances()' \
                % self.__class__.__name__)
                
        model.path.update({'CreationClassName': None, 'Name': None})
        
        model['CreationClassName'] = 'SUSE_SystemOwner'    
        #model['Name'] = '' # TODO (type = unicode)
        if keys_only:
            yield model
        else:
            try:
                yield self.get_instance(env, model)
            except pywbem.CIMError, (num, msg):
                if num not in (pywbem.CIM_ERR_NOT_FOUND, 
                               pywbem.CIM_ERR_ACCESS_DENIED):
                    raise


    def set_instance(self, env, instance, modify_existing):
        """
        Return a newly created or modified instance.
        """

        logger = env.get_logger()
        logger.log_debug('Entering %s.set_instance()' % self.__class__.__name__)
        raise pywbem.CIMError(pywbem.CIM_ERR_ACCESS_DENIED)
        return instance


    def delete_instance(self, env, instance_name):
        """
        Delete an instance.
        """ 

        logger = env.get_logger()
        logger.log_debug('Entering %s.delete_instance()' % self.__class__.__name__)
        raise pywbem.CIMError(pywbem.CIM_ERR_ACCESS_DENIED)
        

def get_providers(env): 
    suse_systemowner_prov = SUSE_SystemOwner(env)  
    return {'SUSE_SystemOwner': suse_systemowner_prov}
