"""Python Provider for SUSE_SystemOwner

Instruments the CIM class SUSE_SystemOwner

"""

import pywbem
from pywbem.cim_provider2 import CIMProvider2

class SUSE_SystemOwner(CIMProvider2):
    """Instrument the CIM class SUSE_SystemOwner 

    Return OS owner data
    
    """

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
        

        # TODO fetch system resource matching the following keys:
        #   model['CreationClassName']
        #   model['Name']

        #model['BusinessCategory'] = '' # TODO 
        #model['Caption'] = '' # TODO 
        #model['CommonName'] = '' # TODO (Required)
        #model['Description'] = '' # TODO 
        #model['ElementName'] = '' # TODO 
        #model['EmployeeNumber'] = '' # TODO 
        #model['EmployeeType'] = '' # TODO 
        #model['FacsimileTelephoneNumber'] = '' # TODO 
        #model['GivenName'] = '' # TODO 
        #model['HomePhone'] = '' # TODO 
        #model['HomePostalAddress'] = ['',] # TODO 
        #model['InstanceID'] = '' # TODO 
        #model['JPEGPhoto'] = '' # TODO 
        #model['LocalityName'] = '' # TODO 
        #model['Mail'] = '' # TODO 
        #model['Manager'] = '' # TODO 
        #model['Mobile'] = '' # TODO 
        #model['OU'] = '' # TODO 
        #model['OwnerContact'] = '' # TODO 
        #model['OwnerDescription'] = '' # TODO 
        #model['OwnerName'] = '' # TODO 
        #model['Pager'] = '' # TODO 
        #model['PostalAddress'] = ['',] # TODO 
        #model['PostalCode'] = '' # TODO 
        #model['PreferredLanguage'] = '' # TODO 
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
        logger.log_debug('Entering %s.set_instance()' \
                % self.__class__.__name__)
        # TODO create or modify the instance
        raise pywbem.CIMError(pywbem.CIM_ERR_NOT_SUPPORTED) # Remove to implement
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
        logger.log_debug('Entering %s.delete_instance()' \
                % self.__class__.__name__)

        # TODO delete the resource
        raise pywbem.CIMError(pywbem.CIM_ERR_NOT_SUPPORTED) # Remove to implement
        
## end of class SUSE_SystemOwnerProvider
    
## get_providers() for associating CIM Class Name to python provider class name
    
def get_providers(env): 
    suse_systemowner_prov = SUSE_SystemOwner(env)  
    return {'SUSE_SystemOwner': suse_systemowner_prov}
