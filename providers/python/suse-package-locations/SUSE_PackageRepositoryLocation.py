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
    """
    Instrument the CIM class SUSE_PackageRepositoryLocation 
    Return packages locations in the OS and the packaging system.
    """

    def __init__ (self, env):
        logger = env.get_logger()
        logger.log_debug('Initializing provider %s from %s' % (self.__class__.__name__, __file__))


    def get_instance(self, env, model, repo=None):
        """
        Return an instance.
        """

        # Enumerating instances only
        if not repo:
            raise pywbem.CIMError(pywbem.CIM_ERR_NOT_FOUND)

        logger = env.get_logger()
        logger.log_debug('Entering %s.get_instance()' \
                % self.__class__.__name__)
        

        # TODO fetch system resource matching the following keys:
        #   model['Antecedent']
        #   model['Dependent']

        model['IsAutoRefresh'] = repo.auto_refresh
        model['IsEnabled'] = repo.enabled
        model['PackageManager'] = repo.pkg_manager
        model['RepoType'] = repo.pkg_type
        model['URL'] = repo.url # TODO: deal with the "repo.path" for Zypper
        model['Name'] = repo.name
        
        return model


    def enum_instances(self, env, model, keys_only):
        """
        Enumerate instances.
        """

        logger = env.get_logger()
        logger.log_debug('Entering %s.enum_instances()' \
                % self.__class__.__name__)
                
        model.path.update({'Dependent': None, 'Antecedent': None})

        for repo in RepoReader().get_repositories():
            model['Antecedent'] = pywbem.CIMInstanceName(classname='CIM_PhysicalElement')
            model['Dependent'] = pywbem.CIMInstanceName(classname='CIM_System')
            if keys_only:
                yield model
            else:
                try:
                    yield self.get_instance(env, model, repo)
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
        """
        Instrument Associations.
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
