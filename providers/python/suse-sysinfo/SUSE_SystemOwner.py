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
            for owner_node in d.getElementsByTagName("node")[0].getElementsByTagName("owner"):
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
        """Return an instance.

        Keyword arguments:
        env -- Provider Environment (pycimmb.ProviderEnvironment)
        model -- A template of the pywbem.CIMInstance to be returned.  The 
            key properties are set on this instance to correspond to the 
            instanceName that was requested.  The properties of the model
            are already filtered according to the PropertyList from the 
            request.  Only properties present in the model need to be
            given values.  If you prefer, you can set all of the 
            values, and the instance will be filtered for you. 

        Possible Errors:
        CIM_ERR_ACCESS_DENIED
        CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
            or otherwise incorrect parameters)
        CIM_ERR_NOT_FOUND (the CIM Class does exist, but the requested CIM 
            Instance does not exist in the specified namespace)
        CIM_ERR_FAILED (some other unspecified error occurred)

        """
        
        logger = env.get_logger()
        logger.log_debug('Entering %s.get_instance()' \
                % self.__class__.__name__)
        
        lc = self.get_system_locale()
        owner = self.get_node_owner()

        # TODO fetch system resource matching the following keys:
        #   model['CreationClassName']
        #   model['Name']

        #model['BusinessCategory'] = '' # TODO 
        #model['Caption'] = '' # TODO 
        #model['CommonName'] = '' # TODO (Required)
        model['Description'] = owner.memo
        #model['ElementName'] = '' # TODO 
        #model['EmployeeNumber'] = '' # TODO 
        #model['EmployeeType'] = '' # TODO 
        #model['FacsimileTelephoneNumber'] = '' # TODO 
        #model['GivenName'] = '' # TODO 
        #model['HomePhone'] = '' # TODO 
        #model['HomePostalAddress'] = ['',] # TODO 
        #model['InstanceID'] = '' # TODO 
        #model['JPEGPhoto'] = '' # TODO 
        model['LocalityName'] = lc[-1]
        model['Mail'] = "root@" + socket.gethostname()
        #model['Manager'] = '' # TODO 
        #model['Mobile'] = '' # TODO 
        #model['OU'] = '' # TODO 
        model['OwnerContact'] = owner.contact.email
        model['OwnerDescription'] = owner.description
        model['OwnerName'] = owner.name
        #model['Pager'] = '' # TODO 
        #model['PostalAddress'] = ['',] # TODO 
        #model['PostalCode'] = '' # TODO 
        model['PreferredLanguage'] = lc[0]
        #model['Secretary'] = '' # TODO 
        #model['StateOrProvince'] = '' # TODO 
        #model['Surname'] = '' # TODO (Required)
        #model['TelephoneNumber'] = '' # TODO 
        #model['Title'] = '' # TODO 
        #model['UserID'] = '' # TODO 

        return model


    def enum_instances(self, env, model, keys_only):
        """Enumerate instances.

        The WBEM operations EnumerateInstances and EnumerateInstanceNames
        are both mapped to this method. 
        This method is a python generator

        Keyword arguments:
        env -- Provider Environment (pycimmb.ProviderEnvironment)
        model -- A template of the pywbem.CIMInstances to be generated.  
            The properties of the model are already filtered according to 
            the PropertyList from the request.  Only properties present in 
            the model need to be given values.  If you prefer, you can 
            always set all of the values, and the instance will be filtered 
            for you. 
        keys_only -- A boolean.  True if only the key properties should be
            set on the generated instances.

        Possible Errors:
        CIM_ERR_FAILED (some other unspecified error occurred)

        """

        logger = env.get_logger()
        logger.log_debug('Entering %s.enum_instances()' \
                % self.__class__.__name__)
                
        # Prime model.path with knowledge of the keys, so key values on
        # the CIMInstanceName (model.path) will automatically be set when
        # we set property values on the model. 
        model.path.update({'CreationClassName': None, 'Name': None})
        
        while False: # TODO more instances?
            # TODO fetch system resource
            # Key properties    
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
        """Return a newly created or modified instance.

        Keyword arguments:
        env -- Provider Environment (pycimmb.ProviderEnvironment)
        instance -- The new pywbem.CIMInstance.  If modifying an existing 
            instance, the properties on this instance have been filtered by 
            the PropertyList from the request.
        modify_existing -- True if ModifyInstance, False if CreateInstance

        Return the new instance.  The keys must be set on the new instance. 

        Possible Errors:
        CIM_ERR_ACCESS_DENIED
        CIM_ERR_NOT_SUPPORTED
        CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
            or otherwise incorrect parameters)
        CIM_ERR_ALREADY_EXISTS (the CIM Instance already exists -- only 
            valid if modify_existing is False, indicating that the operation
            was CreateInstance)
        CIM_ERR_NOT_FOUND (the CIM Instance does not exist -- only valid 
            if modify_existing is True, indicating that the operation
            was ModifyInstance)
        CIM_ERR_FAILED (some other unspecified error occurred)

        """

        logger = env.get_logger()
        logger.log_debug('Entering %s.set_instance()' % self.__class__.__name__)
        raise pywbem.CIMError(pywbem.CIM_ERR_ACCESS_DENIED)
        return instance


    def delete_instance(self, env, instance_name):
        """Delete an instance.

        Keyword arguments:
        env -- Provider Environment (pycimmb.ProviderEnvironment)
        instance_name -- A pywbem.CIMInstanceName specifying the instance 
            to delete.

        Possible Errors:
        CIM_ERR_ACCESS_DENIED
        CIM_ERR_NOT_SUPPORTED
        CIM_ERR_INVALID_NAMESPACE
        CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
            or otherwise incorrect parameters)
        CIM_ERR_INVALID_CLASS (the CIM Class does not exist in the specified 
            namespace)
        CIM_ERR_NOT_FOUND (the CIM Class does exist, but the requested CIM 
            Instance does not exist in the specified namespace)
        CIM_ERR_FAILED (some other unspecified error occurred)

        """ 

        logger = env.get_logger()
        logger.log_debug('Entering %s.delete_instance()' % self.__class__.__name__)
        raise pywbem.CIMError(pywbem.CIM_ERR_ACCESS_DENIED)
        

def get_providers(env): 
    suse_systemowner_prov = SUSE_SystemOwner(env)  
    return {'SUSE_SystemOwner': suse_systemowner_prov}
