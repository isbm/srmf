/*
 * Author: Bo Maryniuk <bo@suse.de>
 *
 * Copyright (c) 2013 Bo Maryniuk. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *     3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY BO MARYNIUK "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package de.suse.srmf.lib.client;

import de.suse.srmf.lib.client.export.SRMFLocalStorage;
import de.suse.srmf.lib.client.export.SRMFMessage;
import de.suse.srmf.lib.client.export.SRMFRenderMap;
import de.suse.srmf.lib.client.export.SRMFStorage;
import de.suse.srmf.lib.client.export.SRMFUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.cim.CIMInstance;
import javax.cim.CIMObjectPath;
import javax.security.auth.Subject;
import javax.wbem.CloseableIterator;
import javax.wbem.WBEMException;
import javax.wbem.client.PasswordCredential;
import javax.wbem.client.UserPrincipal;
import javax.wbem.client.WBEMClient;
import javax.wbem.client.WBEMClientConstants;
import javax.wbem.client.WBEMClientFactory;
import org.sblim.cimclient.CIMXMLTraceListener;
import org.sblim.cimclient.internal.cimxml.CIMClientXML_HelperImpl;
import org.sblim.cimclient.internal.cimxml.CimXmlSerializer;
import org.sblim.cimclient.internal.logging.LogAndTraceBroker;



/**
 * Metadata of the message.
 * 
 * @author bo
 */
class SRMFMessageMeta {
    private String providerBaseClass;
    private String providerClass;
    private String namespace;
    private String objectId;

    public SRMFMessageMeta(String objectId, String providerBaseClass, String providerClass, String namespace) {
        this.objectId = objectId;
        this.providerBaseClass = providerBaseClass;
        this.providerClass = providerClass;
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getProviderBaseClass() {
        return providerBaseClass;
    }

    public String getProviderClass() {
        return providerClass;
    }

    public String getObjectId() {
        return objectId;
    }
}


/**
 *
 * @author bo
 */
public class CIMClientLib {
    public static final String SRMF_MAP_FILE = "srmf-map.xml";
    public static final String SRMF_OBJ_FILE = "srmf-objects.xml";
    public static final String SRMF_UNIX_DEFAULT_CONF_PATH = "/etc/srmf/conf";

    private WBEMClient client;
    private String namespace;
    private boolean traceMode;
    private SRMFStorage localStorage;
    private SRMFRenderMap exportSRMFRenderMap;
    private String currentSRMFMapRefID;
    private SRMFMessageMeta currentSRMFMessageMeta;


    /**
     * Constructor.
     * 
     * @param location
     * @throws Exception 
     */
    public CIMClientLib(String hostname, SRMFConfig setup) throws Exception {
        // Install XML sniffer
        CIMXMLTraceListener xmlListener = new CIMXMLTraceListener() {
            @Override
            public void traceCIMXML(Level level, String message, boolean isRequest) {
                if (!isRequest && CIMClientLib.this.traceMode) {
                    message = "<?xml" + message.split("\\<\\?xml", 2)[1].split("</CIM>")[0] + "</CIM>";
                    
                    // Render
                    if (CIMClientLib.this.currentSRMFMapRefID != null) {
                        try {
                            System.err.println(SRMFUtils.xproc(SRMFUtils.getXMLDocumentFromString(message),
                                                               CIMClientLib.this.exportSRMFRenderMap.getRenderingStyle(CIMClientLib.this.currentSRMFMapRefID)));
                            CIMClientLib.this.currentSRMFMapRefID = null;
                        } catch (Exception ex) {
                            Logger.getLogger(CIMClientLib.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                    // Export
                    else if (CIMClientLib.this.currentSRMFMessageMeta != null) {
                        try {
                            CIMClientLib.this.localStorage.storeMessage(new SRMFMessage(CIMClientLib.this.currentSRMFMessageMeta.getObjectId(),
                                                                                        CIMClientLib.this.currentSRMFMessageMeta.getProviderBaseClass(),
                                                                                        CIMClientLib.this.currentSRMFMessageMeta.getProviderBaseClass(),
                                                                                        message));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            System.err.println("Error writing provider data: " + ex.getLocalizedMessage());
                        }

                        CIMClientLib.this.currentSRMFMessageMeta = null;
                    }

                    // STDOUT
                    else {
                        System.out.println(message);
                    }
                }
            }
        };
        LogAndTraceBroker.getBroker().addCIMXMLTraceListener(xmlListener);

        // Local storage
        this.localStorage = new SRMFLocalStorage(new File(setup.getItem(".srmf.manifest.path", SRMFLocalStorage.DEFAULT_STORAGE_PATH)),
                                                 setup.getItem(".srmf.manifest.compression", "enabled").toLowerCase().equals("enabled"));

        // Render map
        this.exportSRMFRenderMap = new SRMFRenderMap(new File(setup.getItem(".srmf.manifest.renderers",
                                                                            SRMFRenderMap.DEFAUILT_SRMF_RENDER_PATH)));
        File srmfMap = new File(String.format("%s/%s", CIMClientLib.SRMF_UNIX_DEFAULT_CONF_PATH, CIMClientLib.SRMF_MAP_FILE));
        File srmfObjects = new File(String.format("%s/%s", CIMClientLib.SRMF_UNIX_DEFAULT_CONF_PATH, CIMClientLib.SRMF_OBJ_FILE));
        this.exportSRMFRenderMap.loadFromFile(srmfMap.exists() ? srmfMap : new File(CIMClientLib.SRMF_MAP_FILE),
                                              srmfObjects.exists() ? srmfObjects : new File(CIMClientLib.SRMF_OBJ_FILE));

        // CIM Client
        URL location = setup.getConnectionUrl(hostname);
        this.client = WBEMClientFactory.getClient(WBEMClientConstants.PROTOCOL_CIMXML);
        CIMObjectPath path = new CIMObjectPath(location.getProtocol(),
                location.getHost(), String.valueOf(location.getPort()), null, null, null);
        Subject subj = new Subject();
        subj.getPrincipals().add(new UserPrincipal(setup.getUsername(hostname)));
        subj.getPrivateCredentials().add(new PasswordCredential(setup.getPassword(hostname)));
        this.client.initialize(path, subj, new Locale[]{Locale.US});
    }


    /**
     * Converts result from the object name through the internal 
     * mapping "ObjectName -> rendered result type".
     * 
     * @param objectName
     * @param type 
     */
    public void convertResult(String objectName, String type) {
    }


    /**
     * Set the model namespace.
     * @param namespace 
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Inspect objects.
     * @param objects 
     */
    public void doInspectObjects(String ... objects) {
        this.traceMode = true;
        for (String objName : objects) {
            try {
                CloseableIterator<CIMInstance> it = client.execQuery(new CIMObjectPath("", this.namespace),
                                                                     "select * from " + objName, "WQL");
                while (it.hasNext()){
                    CIMInstance instance = it.next();
                    //System.out.println(instance);
                }
                it.close();
            } catch (Exception ex) {
                System.err.println(String.format(">>> Failed to process %s provider!", objName));
            }
        }
        this.traceMode = false;
    }


    /**
     * Get all available classes.
     * 
     * @param namespace 
     */
    public void doEnumerateClasses() throws WBEMException {
        CIMObjectPath rootPath = new CIMObjectPath("", this.namespace);
        CloseableIterator<CIMObjectPath> cimpathIterator = this.client.enumerateClassNames(rootPath, true);
        while (cimpathIterator.hasNext()) {
            CIMObjectPath objectPath = cimpathIterator.next();
            System.err.println(objectPath.getObjectName());
        }
    }

    /**
     * Show all available CMS exports.
     */
    public void doShowAvailableCMSExports() throws Exception {
        List<SRMFRenderMap.SRMFMapDestination> renderIds = this.exportSRMFRenderMap.getSupportedRenderIDs();
        if (renderIds.isEmpty()) {
            throw new Exception("Error: No destinatination export renders are available.");
        }

        List<List> table = new ArrayList<List>();
        List<String> header = new ArrayList<String>();
        header.add("ID");
        header.add("Title");
        table.add(header);
        
        for (int i = 0; i < renderIds.size(); i++) {
            SRMFRenderMap.SRMFMapDestination destination = renderIds.get(i);
            List<String> row = new ArrayList<String>();
            row.add(destination.getName());
            row.add(destination.getTitle());
            table.add(row);
        }
        
        SRMFUtils.printTable(table, System.out);
    }


    /**
     * Makes a snapshot of the manifest to the current state (no versions support).
     * 
     * @throws Exception 
     */
    public void doManifestSnapshot() throws Exception {
        this.traceMode = true;
        List<SRMFRenderMap.SRMFMapProvider> providers = this.exportSRMFRenderMap.getProviders();
        for (int i = 0; i < providers.size(); i++) {
            SRMFRenderMap.SRMFMapProvider sRMFMapProvider = providers.get(i);
            this.currentSRMFMessageMeta = new SRMFMessageMeta(sRMFMapProvider.getId(),
                                                              sRMFMapProvider.getObjectClass(), // XXX: Traverse base class in the future.
                                                              sRMFMapProvider.getObjectClass(),
                                                              sRMFMapProvider.getNamespace());
            client.execQuery(new CIMObjectPath("", sRMFMapProvider.getNamespace()), sRMFMapProvider.getQuery(), "WQL");
        }
        this.traceMode = false;
    }


    /**
     * Export to the destination.
     * @param get 
     */
    public void doExportToDestination(String[] destinationIdArr) throws Exception {
        String destinationId = destinationIdArr[0];
        SRMFRenderMap.SRMFMapDestination dst = null;
        List<SRMFRenderMap.SRMFMapDestination> destinations = this.exportSRMFRenderMap.getSupportedRenderIDs();
        for (int i = 0; i < destinations.size(); i++) {
            SRMFRenderMap.SRMFMapDestination sRMFMapDest = destinations.get(i);
            if (sRMFMapDest.getName().equals(destinationId)) {
                dst = sRMFMapDest;
                break;
            }
        }
        if (dst == null) {
            throw new Exception(String.format("Destination \"%s\" not recognized.", destinationId));
        }

        System.err.println("Exporting to " + destinationId);
        
        this.traceMode = true;
        List<SRMFRenderMap.SRMFMapDestination.SRMFMapRef> references = dst.getReferences();
        for (int i = 0; i < references.size(); i++) {
            SRMFRenderMap.SRMFMapDestination.SRMFMapRef sRMFMapRef = references.get(i);
            SRMFRenderMap.SRMFMapProvider sRMFMapProvider = this.exportSRMFRenderMap.getProviderByID(sRMFMapRef.getId());
            try {
                this.currentSRMFMapRefID = sRMFMapRef.getRender();
                CloseableIterator<CIMInstance> it = client.execQuery(new CIMObjectPath("", sRMFMapProvider.getNamespace()),
                                                                     sRMFMapProvider.getQuery(), "WQL");
            } catch (Exception ex) {
                System.err.println(String.format("Failed to render \"%s\" provider for %s!", sRMFMapProvider.getId(), dst.getTitle()));
            }
            this.traceMode = false;
        }
    }
    

    /**
     * Process compound object.
     * 
     * @param query 
     */
    public void doQuery(String query) {
        this.traceMode = true;
        try {
            client.execQuery(new CIMObjectPath("", this.namespace), query, "WQL");
        } catch (Exception ex) {
            System.err.println(String.format(">>> Failed to process \"%s\" query!", query));
        }

        this.traceMode = false;
    }

    
    /**
     * Enumerates all the class instances
     * @param className 
     */
    public void enumClassInstances(String className) {
        try {
            this.traceMode = true;
            CloseableIterator<CIMInstance> it = this.client.enumerateInstances(new CIMObjectPath(className, this.namespace), true, false, true, null);
                //while (it.hasNext()){
                //    System.out.println(it.next());
                //}        
                it.close();
        } catch (WBEMException ex) {
            Logger.getLogger(CIMClientLib.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.traceMode = false;
    }

    /**
     * Serialize CIM instance.
     * 
     * @param instance
     * @throws Exception 
     */
    public void serializeInstance(CIMInstance instance) throws Exception {
        CIMClientXML_HelperImpl x = new CIMClientXML_HelperImpl();
        CimXmlSerializer.serialize(System.out, x.createInstance_request(x.newDocument(), instance.getObjectPath(), instance), true);
    }


    /**
     * Usage.
     */
    public static void usage() {
        System.err.println("Usage: <operations>");
        System.err.println("Operations:");
        System.err.println("\t--hostname=<hostname>\t\tHostname to use in the config file.");
        System.err.println("\t--config=/path/to/config\tUsed /etc/srmf.conf or ./srmf.conf by default.");
        System.err.println("\t--show-classes\t\t\tShow available classes on the system.");
        System.err.println("\t--describe=<Object,Object...>\tDescribe an array of objects (or just one).");
        System.err.println("\t--namespace=<value>\t\tNamespace on the target machine.");
        System.err.println("\t--export=<value>\t\tExport for deployment with particular CMS.");
        System.err.println("\t--query={...}\t\t\tWQL query block.");
        System.err.println("\t--snapshot\t\t\tSnapshot current service manifest.");
        System.err.println("\t--available-cms\t\t\tList of supported CMS.");
        System.err.println("\t--trace\t\t\t\tShow extended tracebacks of errors.\n");
        System.err.println("\t--help\t\t\t\tThis help text.\n");
        // --test    Anything for trying something :-)
        System.exit(0);
    }
    
    /**
     * Main.
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) {
        Map<String, String[]> params = null;
        try {
            params = SRMFUtils.getArgs(args, "show-classes", "available-cms", "snapshot", "help", "trace");
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getLocalizedMessage());
            System.exit(0);
        }

        if (params.isEmpty() || params.get("help") != null) {
            CIMClientLib.usage();
        }

        try{
            SRMFUtils.checkParams(params,
                                  new String[]{"hostname"},
                                  new String[]{"describe", "show-classes", 
                                               "export", "available-cms",
                                               "snapshot", "test", "query"});
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getLocalizedMessage());
            System.exit(0);
        }
        
        if (!params.containsKey("namespace")) {
            params.put("namespace", new String[]{"root/cimv2"});
        }
        
        if (params.containsKey("help")) {
            CIMClientLib.usage();
        }

        /*
        File setupFile = new File(params.get("config") != null ? params.get("config")[0] : "/etc/srmf.conf");
        if (!setupFile.exists()) {
            try {
                System.err.println(String.format("Warning: %s does not exists. Using default in current directly.", setupFile.getCanonicalPath()));
            } catch (IOException ex) {
                if (params.containsKey("trace")) {
                    Logger.getLogger(CIMClientLib.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            setupFile = new File("srmf.conf");
        }

        if (!setupFile.exists()) {
            System.err.println(String.format("Error: file %s does not exists!", setupFile.getAbsolutePath()));
            System.exit(0);
        }
        
        Properties setup = new Properties();
        try {
            setup.load(new FileInputStream(setupFile));
        } catch (IOException ex) {
            if (params.containsKey("trace")) {
                Logger.getLogger(CIMClientLib.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.err.println("Error loading configuration: " + ex.getLocalizedMessage());
            System.exit(0);
        }
        */

        // Run! :)
        try {
            CIMClientLib cimclient = new CIMClientLib(params.get("hostname")[0], 
                                                      new SRMFConfig(params.get("config") != null ? params.get("config")[0] : null));
            cimclient.setNamespace(params.get("namespace")[0]);
            
            if (params.containsKey("snapshot")) {
                cimclient.doManifestSnapshot();
            } else if (params.containsKey("available-cms")) {
                cimclient.doShowAvailableCMSExports();
            } else if (params.containsKey("export")) {
                cimclient.doExportToDestination(params.get("export"));
            } else if (params.containsKey("describe")) {
                cimclient.doInspectObjects(params.get("describe"));
            } else if (params.containsKey("show-classes")) {
                cimclient.doEnumerateClasses();
            } else if (params.containsKey("query")) {
                cimclient.doQuery(params.get("query")[0]);
            } else if (params.containsKey("test")) {
                cimclient.enumClassInstances(params.get("test")[0]);
            } else {
                CIMClientLib.usage();
                System.err.println("Error:\n\tWrong parameters.");
            }
        } catch (Exception ex) {
            if (params.containsKey("trace")) {
                Logger.getLogger(CIMClientLib.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.err.println("Error: " + ex.getLocalizedMessage());
        }
    }
}
