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

import de.suse.srmf.lib.client.export.ExportDispatcher;
import de.suse.srmf.lib.client.export.RichArrayList;
import de.suse.srmf.lib.client.export.storage.SRMFLocalStorage;
import de.suse.srmf.lib.client.export.SRMFMessage;
import de.suse.srmf.lib.client.export.SRMFRenderMap;
import de.suse.srmf.lib.client.export.SRMFRenderMapResolver;
import de.suse.srmf.lib.client.export.storage.SRMFStorage;
import de.suse.srmf.lib.client.export.SRMFUtils;
import de.suse.srmf.lib.client.export.storage.SRMFOrientDBStorage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    public static final int FILE_STORE = 1;
    public static final int DB_STORE = 2;

    private String providerBaseClass;
    private String providerClass;
    private String namespace;
    private String objectId;
    private Map<Integer, Boolean> flags;

    public SRMFMessageMeta(String objectId, String providerBaseClass, String providerClass, String namespace) {
        this.objectId = objectId;
        this.providerBaseClass = providerBaseClass;
        this.providerClass = providerClass;
        this.namespace = namespace;
        this.flags = new HashMap<Integer, Boolean>();
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
    
    public SRMFMessageMeta setFlag(int flag) {
        this.flags.put(flag, Boolean.TRUE);
        return this;
    }
    
    public SRMFMessageMeta unsetFlag(int flag) {
        if (this.flags.containsKey(flag)) {
            this.flags.remove(flag);
        }
        
        return this;
    }
    
    public Boolean hasFlag(int flag) {
        return this.flags.containsKey(flag);
    }
}


/**
 *
 * @author bo
 */
public class CIMClientLib {
    public static final String SRMF_MAP_FILE = "srmf-map.xml";
    public static final String SRMF_OBJ_FILE = "srmf-index.xml";
    public static final String SRMF_UNIX_DEFAULT_CONF_PATH = "/etc/srmf/conf";

    private WBEMClient client;
    private String targetSystemHostname;
    private SRMFConfig setup;
    private String namespace;
    private boolean traceMode;
    private SRMFStorage localStorage;
    private SRMFRenderMap exportSRMFRenderMap;
    private List<SRMFRenderMap.SRMFMapDestination.SRMFMapRef.SRMFRender> currentSRMFMapRefID;
    private SRMFMessageMeta currentSRMFMessageMeta;


    /**
     * Constructor.
     * 
     * @param location
     * @throws Exception 
     */
    public CIMClientLib(String hostname, SRMFConfig config, URL optionalIndex) throws Exception {
        this.setup = config;

        // Install XML sniffer
        CIMXMLTraceListener xmlListener = new CIMXMLTraceListener() {
            @Override
            public void traceCIMXML(Level level, String message, boolean isRequest) {
                if (!isRequest && CIMClientLib.this.traceMode) {
                    message = "<?xml" + message.split("\\<\\?xml", 2)[1].split("</CIM>")[0] + "</CIM>";
                    
                    // Render
                    if (CIMClientLib.this.currentSRMFMapRefID != null) {
                        try {
                            for (int i = 0; i < CIMClientLib.this.currentSRMFMapRefID.size(); i++) {
                                SRMFRenderMap.SRMFMapDestination.SRMFMapRef.SRMFRender sRMFRender = CIMClientLib.this.currentSRMFMapRefID.get(i);
                                new ExportDispatcher(new File((String) ((RichArrayList) CIMClientLib.this.currentSRMFMapRefID).getAttribute("outputPath")))
                                        .dispatch(SRMFUtils.xproc(SRMFUtils.getXMLDocumentFromString(message),
                                                                  CIMClientLib.this.exportSRMFRenderMap.getRenderingStyle(sRMFRender.getRender())),
                                        (String) ((RichArrayList) CIMClientLib.this.currentSRMFMapRefID).getAttribute("targetSystemHostname"),
                                        sRMFRender.getDestinationDescriptor());
                                System.err.println("Writing " + sRMFRender.getDestinationDescriptor());
                            }
                            CIMClientLib.this.currentSRMFMapRefID = null;
                        } catch (Exception ex) {
                            Logger.getLogger(CIMClientLib.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                    // Export
                    else if (CIMClientLib.this.currentSRMFMessageMeta != null) {
                        try {
                            SRMFMessage msg = new SRMFMessage(
                                    CIMClientLib.this.targetSystemHostname,
                                    CIMClientLib.this.currentSRMFMessageMeta.getObjectId(),
                                    CIMClientLib.this.currentSRMFMessageMeta.getProviderBaseClass(),
                                    CIMClientLib.this.currentSRMFMessageMeta.getProviderBaseClass(),
                                    message
                            );

                            // Store to the filesystem
                            if (CIMClientLib.this.currentSRMFMessageMeta.hasFlag(SRMFMessageMeta.FILE_STORE)) {
                                try {
                                    CIMClientLib.this.localStorage.storeMessage(msg);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    System.err.println("Error writing provider data: " + ex.getLocalizedMessage());
                                }
                            }
                            
                            // Store to the orientdb
                            if (CIMClientLib.this.currentSRMFMessageMeta.hasFlag(SRMFMessageMeta.DB_STORE)) {
                                SRMFOrientDBStorage.getInstance().storeMessage(msg);
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(CIMClientLib.class.getName()).log(Level.SEVERE, null, ex);
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
        this.localStorage = new SRMFLocalStorage(new File(this.setup.getItem(".srmf.manifest.path", SRMFLocalStorage.DEFAULT_STORAGE_PATH)),
                                                 this.setup.getItem(".srmf.manifest.compression", "enabled").toLowerCase().equals("enabled"));
 
        // Render map
        this.exportSRMFRenderMap = new SRMFRenderMap(new File(this.setup.getItem(".srmf.manifest.renderers",
                                                                            SRMFRenderMap.DEFAUILT_SRMF_RENDER_PATH)));
        File srmfMap = new File(String.format("%s/%s", SRMFRenderMapResolver.SRMF_MANIFEST_PATH, CIMClientLib.SRMF_MAP_FILE));
        this.exportSRMFRenderMap.loadFromFile(srmfMap.exists() ? srmfMap : new File(CIMClientLib.SRMF_MAP_FILE), optionalIndex);

        // CIM Client
        URL location = this.setup.getConnectionUrl(hostname);
        this.targetSystemHostname = location.getHost();
        this.client = WBEMClientFactory.getClient(WBEMClientConstants.PROTOCOL_CIMXML);
        CIMObjectPath path = new CIMObjectPath(location.getProtocol(),
                location.getHost(), String.valueOf(location.getPort()), null, null, null);
        Subject subj = new Subject();
        subj.getPrincipals().add(new UserPrincipal(this.setup.getUsername(hostname)));
        subj.getPrivateCredentials().add(new PasswordCredential(this.setup.getPassword(hostname)));
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
                CloseableIterator<CIMInstance> it = this.executeQuery("select * from " + objName, null);
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
        CIMObjectPath rootPath = new CIMObjectPath(null, null, null, this.namespace, "", null);
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
                                                              sRMFMapProvider.getNamespace())
                                                                  //.setFlag(SRMFMessageMeta.FILE_STORE) // XXX: Config!
                                                                  .setFlag(SRMFMessageMeta.DB_STORE);  // XXX: Config!
            try {
                if (sRMFMapProvider.getType().equals(SRMFRenderMap.SRMFMapProvider.ACCESS_TYPE_STATIC)) {
                    this.executeQuery(sRMFMapProvider.getQuery(), sRMFMapProvider.getNamespace());
                } else if (sRMFMapProvider.getType().equals(SRMFRenderMap.SRMFMapProvider.ACCESS_TYPE_INSTANCE)) {
                    this.enumerateInstances(sRMFMapProvider.getObjectClass(), sRMFMapProvider.getNamespace());
                } else {
                    System.err.println("ERROR: Skipping '%s' (%s) - Unknown access type.".format(sRMFMapProvider.getTitle(), sRMFMapProvider.getObjectClass()));
                }
            } catch (WBEMException ex) {
                System.err.println("Error processing '%s' (%s): %s".format(sRMFMapProvider.getTitle(), sRMFMapProvider.getId(), ex.getLocalizedMessage()));
            }
        }
        this.traceMode = false;
    }


    /**
     * Export to the destination.
     * @param get 
     */
    public void doExportToDestination(String[] destinationIdArr, String[] outputPath) throws Exception {
        String destinationId = destinationIdArr != null ? destinationIdArr[0] : null;
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

        this.traceMode = true;
        List<SRMFRenderMap.SRMFMapDestination.SRMFMapRef> references = dst.getReferences();
        for (int i = 0; i < references.size(); i++) {
            SRMFRenderMap.SRMFMapDestination.SRMFMapRef sRMFMapRef = references.get(i);
            SRMFRenderMap.SRMFMapProvider sRMFMapProvider = this.exportSRMFRenderMap.getProviderByID(sRMFMapRef.getId());
            try {
                this.currentSRMFMapRefID = sRMFMapRef.getRenderers();
                ((RichArrayList) this.currentSRMFMapRefID).addAttribute("outputPath", this.setup.getItem(".srmf.manifest.export",
                                                                                                         outputPath != null ? outputPath[0] : null));
                ((RichArrayList) this.currentSRMFMapRefID).addAttribute("targetSystemHostname", this.targetSystemHostname);
                if (sRMFMapProvider.getType().equals(SRMFRenderMap.SRMFMapProvider.ACCESS_TYPE_STATIC)) {
                    this.executeQuery(sRMFMapProvider.getQuery(), sRMFMapProvider.getNamespace());
                } else if (sRMFMapProvider.getType().equals(SRMFRenderMap.SRMFMapProvider.ACCESS_TYPE_INSTANCE)) {
                    this.enumerateInstances(sRMFMapProvider.getObjectClass(), sRMFMapProvider.getNamespace());
                } else {
                    System.err.println("ERROR: Skipping '%s' (%s) - Uknown access type.".format(sRMFMapProvider.getTitle(), sRMFMapProvider.getId()));
                }
            } catch (Exception ex) {
                System.err.println(String.format("Failed to render \"%s\" provider for %s!", sRMFMapProvider.getId(), dst.getTitle()));
            }
            this.traceMode = false;
        }
    }
    
    
    /**
     * Execute query.
     * 
     * @param query
     * @param namespace
     * @throws WBEMException 
     */
    private CloseableIterator<CIMInstance> executeQuery(String query, String namespace) throws WBEMException {
        return this.client.execQuery(new CIMObjectPath(null, null, null,
                (namespace == null ? this.namespace : namespace), "", null), query, "WQL");
    }

    /**
     * Enumerate class instances.
     * 
     * @param className
     * @param namespace
     * @return
     * @throws WBEMException 
     */
    private CloseableIterator<CIMInstance> enumerateInstances(String className, String namespace) throws WBEMException {
        return this.client.enumerateInstances(new CIMObjectPath(null, null, null, 
                (namespace == null ? this.namespace : namespace), className, null), true, false, true, null);
    }

    /**
     * Process compound object.
     * 
     * @param query 
     */
    public void doQuery(String query) {
        this.traceMode = true;
        try {
            this.executeQuery(query, null);
        } catch (Exception ex) {
            System.err.println(String.format(">>> Failed to process \"%s\" query!", query));
        }

        this.traceMode = false;
    }

    
    /**
     * Enumerates all the class instances
     * @param className 
     */
    public void doEnumerateClassInstances(String className) {
        this.traceMode = true;
        try {
            this.enumerateInstances(className, null);
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
        System.err.println("SrMF v0.1, Copyright (c) SUSE Linux Products GmbH");
        System.err.println("\nCommon:");
        System.err.println("\t--hostname=<hostname>\t\tHostname to use in the config file.");
        System.err.println("\t--config=/path/to/config\tUsed /etc/srmf.conf or ./srmf.conf by default.");
        System.err.println("\t--namespace=<value>\t\tNamespace on the target machine.");

        System.err.println("\nDiscovery:");
        System.err.println("\t--show-classes\t\t\tShow available classes on the system.");
        System.err.println("\t--describe=<Object,Object...>\tDescribe an array of objects (or just one).");
        System.err.println("\t--query={...}\t\t\tWQL query block.");
        System.err.println("\t--index-url\t\t\tPass custom index URL.");

        System.err.println("\nExport:");
        System.err.println("\t--export=<value>\t\tExport for deployment with particular CMS.");
        System.err.println("\t--output-path\t\tSpecify custom output path for export.");
        System.err.println("\t--snapshot\t\t\tSnapshot current service manifest.");
        System.err.println("\t--available-cms\t\t\tList of supported CMS.");

        System.err.println("\nOther:");
        System.err.println("\t--trace\t\t\t\tShow extended tracebacks of errors.");
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
        System.setProperty("sblim.wbem.httpPoolSize", "0");
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

        // Run! :)
        try {
            CIMClientLib cimclient = new CIMClientLib(params.get("hostname")[0], 
                                                      SRMFConfig.initialize(params.get("config") != null ? params.get("config")[0] : null),
                                                      params.get("index-url") != null ? new URL(params.get("index-url")[0]) : null);
            cimclient.setNamespace(params.get("namespace")[0]);
            
            if (params.containsKey("snapshot")) {
                cimclient.doManifestSnapshot();
            } else if (params.containsKey("available-cms")) {
                cimclient.doShowAvailableCMSExports();
            } else if (params.containsKey("export")) {
                cimclient.doExportToDestination(params.get("export"), params.get("output-path"));
            } else if (params.containsKey("describe")) {
                cimclient.doInspectObjects(params.get("describe"));
            } else if (params.containsKey("show-classes")) {
                cimclient.doEnumerateClasses();
            } else if (params.containsKey("query")) {
                cimclient.doQuery(params.get("query")[0]);
            } else if (params.containsKey("test")) {
                cimclient.doEnumerateClassInstances(params.get("test")[0]);
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
