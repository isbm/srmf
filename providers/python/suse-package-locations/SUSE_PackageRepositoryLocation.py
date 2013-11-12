"""Python Provider for SUSE_PackageRepositoryLocation
Instruments the CIM class SUSE_PackageRepositoryLocation
"""

import pywbem
import os
from pywbem.cim_provider2 import CIMProvider2



class Repository:
    """
    Repository.
    """
    ENABLED = "yes"
    DISABLED = "no"

    def __init__(self):
        self.pkg_type = "generic"
        self.pkg_manager = "generic"
        self.enabled = Repository.DISABLED
        self.auto_refresh = Repository.DISABLED
        self.url = ""



class RepoReader:
    """
    Repository reader.
    """
    ZYPPER = "Zypper"
    YUM = "Yum"
    APT = "APT"
    IPS = "IPS"
    PORTS = "BSD Ports"


    def __init__(self):
        """
        Constructor.
        """
        if os.path.exists("/etc/zypp/repos.d") and os.path.exists("/usr/bin/zypper"):
            self.type = RepoReader.ZYPPER
        else:
            raise Exception("Cannot determine Package Manager") # At least not yet...

    def _get_zypper_repos(self):
        """
        Return all Zypper repositories.
        """
        repos = []
        path = "/etc/zypp/repos.d/"
        for fobj in os.listdir(path):
            repos.append(self._get_zypper_repo(path + fobj))

        return repos


    def _get_zypper_repo(self, path):
        """
        Return zypper repository.
        """
        data = {}
        for line in filter(None, map(lambda l:l.strip(), open(path).readlines())):
            if line.startswith("[") and line.endswith("]"):
                data["id"] = line[1:-1]
                continue

            tokens = line.split("=", 1)
            if len(tokens) != 2:
                continue
            
            if tokens[0] == 'name':
                data["name"] = tokens[1]
            elif tokens[0] == 'enabled':
                data["enabled"] = tokens[1] == 1 and Repository.ENABLED or Repository.DISABLED
            elif tokens[0] == 'autorefresh':
                data["auto_refresh"] = tokens[1] == 1 and Repository.ENABLED or Repository.DISABLED
            elif tokens[0] == 'baseurl':
                data["url"] = tokens[1]
            elif tokens[0] == 'path':
                data["path"] = tokens[1]
            elif tokens[0] == 'type':
                data["type"] = tokens[1]
            elif tokens[0] == 'keeppackages':
                data["keep_packages"] = tokens[1]

        repo = Repository()
        repo.name = data.get("name")
        repo.enabled = data.get("enabled")
        repo.auto_refresh = data.get("auto_refresh")
        repo.url = data.get("url")
        repo.path = data.get("path")
        repo.pkg_type = data.get("type")
        repo.pkg_manager = RepoReader.ZYPPER

        return repo


    def get_repositories(self):
        """
        Return repositories from the local machine.
        """

        if self.type == RepoReader.ZYPPER:
            return self._get_zypper_repos()

        return []



class SUSE_PackageRepositoryLocation(CIMProvider2):
    """Instrument the CIM class SUSE_PackageRepositoryLocation 
    Return packages locations in the OS and the packaging system.
    """

    def __init__ (self, env):
        logger = env.get_logger()
        logger.log_debug('Initializing provider %s from %s' % (self.__class__.__name__, __file__))


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
        #   model['Antecedent']
        #   model['Dependent']

        model['IsAutoRefresh'] = '' # TODO 
        model['IsEnabled'] = '' # TODO 
        model['PackageManager'] = '' # TODO 
        model['RepoType'] = '' # TODO 
        model['URL'] = '' # TODO 
        
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
        model.path.update({'Dependent': None, 'Antecedent': None})

        # Here we iterate the thing
        for repo in RepoReader().get_repositories():
            pass
        
        while False: # TODO more instances?
            #model['Antecedent'] = pywbem.CIMInstanceName(classname='CIM_PhysicalElement', ...) # TODO (type = REF (pywbem.CIMInstanceName(classname='CIM_PhysicalElement', ...))    
            #model['Dependent'] = pywbem.CIMInstanceName(classname='CIM_System', ...) # TODO (type = REF (pywbem.CIMInstanceName(classname='CIM_System', ...))
            if keys_only:
                yield model
            else:
                try:
                    yield self.get_instance(env, model)
                except pywbem.CIMError, (num, msg):
                    if num not in (pywbem.CIM_ERR_NOT_FOUND, pywbem.CIM_ERR_ACCESS_DENIED):
                        raise


    def set_instance(self, env, instance, modify_existing):
        """
        Set instance. This is always denied.
        """
        logger = env.get_logger()
        logger.log_debug('Entering %s.set_instance()' % self.__class__.__name__)

        raise pywbem.CIMError(pywbem.CIM_ERR_NOT_SUPPORTED)

    def delete_instance(self, env, instance_name):
        """
        Delete the instance. This is always denied.
        """
        logger = env.get_logger()
        logger.log_debug('Entering %s.set_instance()' % self.__class__.__name__)

        raise pywbem.CIMError(pywbem.CIM_ERR_NOT_SUPPORTED)


    def references(self, env, object_name, model, result_class_name, role,
                   result_role, keys_only):
        """Instrument Associations.

        All four association-related operations (Associators, AssociatorNames, 
        References, ReferenceNames) are mapped to this method. 
        This method is a python generator

        Keyword arguments:
        env -- Provider Environment (pycimmb.ProviderEnvironment)
        object_name -- A pywbem.CIMInstanceName that defines the source 
            CIM Object whose associated Objects are to be returned.
        model -- A template pywbem.CIMInstance to serve as a model
            of the objects to be returned.  Only properties present on this
            model need to be set. 
        result_class_name -- If not empty, this string acts as a filter on 
            the returned set of Instances by mandating that each returned 
            Instances MUST represent an association between object_name 
            and an Instance of a Class whose name matches this parameter
            or a subclass. 
        role -- If not empty, MUST be a valid Property name. It acts as a 
            filter on the returned set of Instances by mandating that each 
            returned Instance MUST refer to object_name via a Property 
            whose name matches the value of this parameter.
        result_role -- If not empty, MUST be a valid Property name. It acts 
            as a filter on the returned set of Instances by mandating that 
            each returned Instance MUST represent associations of 
            object_name to other Instances, where the other Instances play 
            the specified result_role in the association (i.e. the 
            name of the Property in the Association Class that refers to 
            the Object related to object_name MUST match the value of this 
            parameter).
        keys_only -- A boolean.  True if only the key properties should be
            set on the generated instances.

        The following diagram may be helpful in understanding the role, 
        result_role, and result_class_name parameters.
        +------------------------+                    +-------------------+
        | object_name.classname  |                    | result_class_name |
        | ~~~~~~~~~~~~~~~~~~~~~  |                    | ~~~~~~~~~~~~~~~~~ |
        +------------------------+                    +-------------------+
           |              +-----------------------------------+      |
           |              |  [Association] model.classname    |      |
           | object_name  |  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~    |      |
           +--------------+ object_name.classname REF role    |      |
        (CIMInstanceName) | result_class_name REF result_role +------+
                          |                                   |(CIMInstanceName)
                          +-----------------------------------+

        Possible Errors:
        CIM_ERR_ACCESS_DENIED
        CIM_ERR_NOT_SUPPORTED
        CIM_ERR_INVALID_NAMESPACE
        CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
            or otherwise incorrect parameters)
        CIM_ERR_FAILED (some other unspecified error occurred)

        """

        logger = env.get_logger()
        logger.log_debug('Entering %s.references()' % self.__class__.__name__)
        ch = env.get_cimom_handle()

        if ch.is_subclass(object_name.namespace, 
                          sub=object_name.classname,
                          super='CIM_PhysicalElement') or \
                ch.is_subclass(object_name.namespace,
                               sub=object_name.classname,
                               super='CIM_System'):
            return self.simple_refs(env, object_name, model,
                          result_class_name, role, result_role, keys_only)

    
def get_providers(env):
    """
    Associating CIM Class Name to python provider class name.
    """
    suse_packagerepositorylocation_prov = SUSE_PackageRepositoryLocation(env)  
    return {'SUSE_PackageRepositoryLocation': suse_packagerepositorylocation_prov}
