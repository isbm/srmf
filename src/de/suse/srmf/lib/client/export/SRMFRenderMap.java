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


package de.suse.srmf.lib.client.export;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author bo
 */
public class SRMFRenderMap {
    public static final String DEFAUILT_SRMF_RENDER_PATH = "/etc/srmf/renderers";
    private List<SRMFMapDestination> rmap;
    private Map<String, SRMFMapProvider> rproviders;
    private final File renderers;

    /**
     * Provider description.
     */
    public static class SRMFMapProvider {
        private String namespace;
        private String id;
        private String title;
        private String query;
        private String objectClass;


        public SRMFMapProvider(String path, String id, String title, String query) throws Exception {
            String nsp = null;
            String cls = null;
            if (path.contains(":")) {
                String[] tokens = path.split(":", 2);
                if (tokens.length == 2) {
                    nsp = tokens[0];
                    cls = tokens[1];
                }
            } else {
                nsp = path;
            }

            // Cleanup query
            if (query != null && query.trim().isEmpty()) {
                query = null;
            }
            if (query != null) {
                query = query.trim();
            } else if (query == null && cls != null) {
                query = "SELECT * FROM " + cls;
            } else {
                throw new Exception(String.format("Unable to parse object! Path: %s, ID: %s", path, id));
            }

            this.namespace = nsp;
            this.objectClass = cls;
            this.id = id;
            this.title = title;
            this.query = query;
        }

        public String getObjectClass() {
            return objectClass;
        }

        public String getId() {
            return id;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getQuery() {
            return query;
        }

        public String getTitle() {
            return title;
        }
    }
    
    /**
     * Map destinations
     */
    public static class SRMFMapDestination {
        /**
         * Map rendering reference
         */
        public static class SRMFMapRef {
            private String render;
            private String id;

            public SRMFMapRef(String id, String render) {
                this.id = id;
                this.render = render;
            }

            public String getId() {
                return id;
            }

            public String getRender() {
                return render;
            }
        }
        
        private String name; // ID of the render.
        private String title;
        private List<SRMFMapRef> references;

        /**
         * Constructor.
         * 
         * @param name
         * @param title 
         */
        public SRMFMapDestination(String name, String title) {
            this.name = name;
            this.title = title;
            this.references = new ArrayList<SRMFMapDestination.SRMFMapRef>();
        }

        /**
         * Get the name of the destination.
         * @return 
         */
        public String getName() {
            return name;
        }

        /**
         * Get title of the destination.
         * @return 
         */
        public String getTitle() {
            return title;
        }
        
        
        /**
         * Add reference to the destination render.
         * 
         * @param reference 
         */
        public void addReference(SRMFMapDestination.SRMFMapRef reference) {
            this.references.add(reference);
        }

        /**
         * Get references.
         * 
         * @return 
         */
        public List<SRMFMapRef> getReferences() {
            return Collections.unmodifiableList(this.references);
        }
    }


    /**
     * Render map constructor.
     * 
     * @param renderMapXMLSource 
     */
    public SRMFRenderMap(File renderers) {
        this.renderers = renderers;
        this.rmap = new ArrayList<SRMFMapDestination>();
        this.rproviders = new HashMap<String, SRMFMapProvider>();
    }
    
    /**
     * Load mapping from the file.
     * 
     * @param mapping 
     */
    public void loadFromFile(File mapping, URL providers)
            throws IOException,
                   ParserConfigurationException,
                   SAXException,
                   Exception {
        this.init(SRMFUtils.getXMLDocumentFromFile(mapping), providers);
    }


    /**
     * Init the mapping.
     * 
     * @param doc 
     */
    private void init(Document mapping, URL providers) throws Exception {
        // Load providers
        this.rproviders = new SRMFRenderMapResolver(providers).getProviders();
        /*
        NodeList objectsNodeList = providers.getElementsByTagName("object");
        for (int i = 0; i < objectsNodeList.getLength(); i++) {
            Element objElement = (Element) objectsNodeList.item(i);
            SRMFMapProvider provider = new SRMFMapProvider(objElement.getAttribute("path"), 
                                                           objElement.getAttribute("id"),
                                                           objElement.getAttribute("title"),
                                                           this.getQuery(objElement));
            this.rproviders.put(provider.getId(), provider);
        }
        */
        
        // Load mapping
        this.rmap.clear();
        NodeList destinationNodeList = mapping.getElementsByTagName("destination");
        for (int i = 0; i < destinationNodeList.getLength(); i++) {
            Element destElement = (Element) destinationNodeList.item(i);
            SRMFMapDestination destination = new SRMFMapDestination(destElement.getAttribute("name"),
                                                                    destElement.getAttribute("title"));
            NodeList refNodeList = destElement.getElementsByTagName("ref");
            for (int j = 0; j < refNodeList.getLength(); j++) {
                Element refElement = (Element) refNodeList.item(j);
                destination.addReference(new SRMFMapDestination.SRMFMapRef(refElement.getAttribute("id"),
                                                                           refElement.getAttribute("render")));
            }
            this.rmap.add(destination);
        }
    }
    
    
    /**
     * Get query from the object element.
     * Currently only first query (all others are ignored).
     * 
     * @param obj
     * @return 
     */
    private String getQuery(Element obj) {
        NodeList queries = obj.getElementsByTagName("query");
        for (int i = 0; i < queries.getLength(); i++) {
            NodeList qnodes = ((Element) queries.item(i)).getChildNodes();
            for (int j = 0; j < qnodes.getLength(); j++) {
                Node qnode = qnodes.item(j);
                if (qnode.getNodeType() == Document.CDATA_SECTION_NODE) {
                    return qnode.getNodeValue().trim();
                }
            }
        }
        
        return null;
    }

    /**
     * Get provider object by ID.
     * 
     * @param id
     * @return 
     */
    public SRMFMapProvider getProviderByID(String id) {
        return this.rproviders.get(id);
    }

    
    /**
     * Get all available providers.
     * 
     * @return 
     */
    public List<SRMFMapProvider> getProviders() {
        return Collections.unmodifiableList(new ArrayList<SRMFMapProvider>(this.rproviders.values()));
    }


    /**
     * Get supported render IDs.
     * 
     * @return 
     */
    public List<SRMFMapDestination> getSupportedRenderIDs() {
        return Collections.unmodifiableList(this.rmap);
    }

    

    /**
     * Get render XSL stylesheet by the ID.
     * 
     * @param destination 
     */
    public File getRenderingStyle(String destinationId) throws IOException {
        File style = new File(this.renderers.getAbsolutePath() + "/" + destinationId + ".xsl");
        if (!style.canRead()) {
            throw new IOException(String.format("File %s is not accessible.", style.getAbsolutePath()));
        }

        return style;
    }
}
