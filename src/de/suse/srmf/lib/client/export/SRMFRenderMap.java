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
 * The render map is the crucial structure in the SRMF.
 * It is core of the CIM operations that allows system administrators
 * operate only at XML level without expanding the core with the Java code at all.
 * The SRMF Render Map is designed to allow combining XML output with XSL templates.
 * 
 * It works one-to-one, one-to-many and many-to-one. In this case it is possible to apply
 * any array of XSL documents to any amount of CIM XML and get any output with multiple
 * or single files.
 * 
 * @author bo
 */
public class SRMFRenderMap {
    public static final String DEFAUILT_SRMF_RENDER_PATH = "/etc/srmf/export";
    private final List<SRMFMapDestination> rmap;
    private Map<String, SRMFMapProvider> rproviders;
    private final File renderers;

    /**
     * Provider description.
     */
    public static class SRMFMapProvider {
        public static final String ACCESS_TYPE_INSTANCE = "instance";
        public static final String ACCESS_TYPE_STATIC = "static";

        private final String namespace;
        private final String id;
        private final String title;
        private final String type;
        private final String query;
        private final String objectClass;


        public SRMFMapProvider(String path, String id, String title, String type, String query) throws Exception {
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
            this.type = type;
            this.query = query;
        }

        public String getObjectClass() {
            return this.objectClass;
        }

        public String getId() {
            return this.id;
        }

        public String getNamespace() {
            return this.namespace;
        }

        public String getQuery() {
            return this.query;
        }

        public String getTitle() {
            return this.title;
        }

        public String getType() {
            return this.type;
        }
    }
    
    /**
     * Map destinations
     */
    public static class SRMFMapDestination {
        /**
         * Set of references that are merged into one output.
         */
        public static class SRMFMergeSet {
            private final String render;
            private final String outDestinationDescriptor;
            private final List<SRMFMapDestination.SRMFMapRef> refSet;

            public SRMFMergeSet(String render, String outDestinationDescriptor) {
                this.render = render;
                this.outDestinationDescriptor = outDestinationDescriptor;
                this.refSet = new ArrayList<SRMFMapRef>();
            }

            /**
             * XSLT render of the merge set.
             * 
             * @return 
             */
            public String getRender() {
                return this.render;
            }

            /**
             * Filename that needs to be generated with this particular action.
             * The full path is calculated by the outsite facility.
             * 
             * @return 
             */
            public String getDestinationDescriptor() {
                return outDestinationDescriptor;
            }


            /**
             * Get the set of the references within the merger.
             * 
             * @return 
             */
            public List<SRMFMapRef> getReferences() {
                return Collections.unmodifiableList(this.refSet);
            }


            /**
             * Add rendering reference to the merge set.
             * 
             * @param reference 
             */
            public void addReference(SRMFMapDestination.SRMFMapRef reference) {
                this.refSet.add(reference);
            }
        }


        /**
         * Map rendering reference
         */
        public static final class SRMFMapRef {
            
            /**
             * Rendering action: XSL to output
             */
            public static final class SRMFRender {
                private final String render;
                private final String outDestinationDescriptor;

                /**
                 * Constructor.
                 * 
                 * @param render
                 * @param outFilename 
                 */
                public SRMFRender(String render, String outFilename) {
                    this.render = render;
                    this.outDestinationDescriptor = outFilename;
                }

                /**
                 * Filename that needs to be generated with this particular action.
                 * The full path is calculated by the outsite facility.
                 * 
                 * @return 
                 */
                public String getDestinationDescriptor() {
                    return outDestinationDescriptor;
                }

                /**
                 * Returns the ID of the XSL file on the system to render the XML output.
                 * @return 
                 */
                public String getRender() {
                    return render;
                }
            }

            private final String id;
            private final List<SRMFRender> renderers;

            /**
             * Constructor.
             * 
             * @param id 
             */
            public SRMFMapRef(String id) {
                this.id = id;
                this.renderers = new RichArrayList<SRMFRender>();
            }

            /**
             * Get ID of the destination.
             * 
             * @return 
             */
            public String getId() {
                return id;
            }
            
            public SRMFMapRef addRender(SRMFRender render) {
                this.renderers.add(render);
                return this;
            }


            /**
             * Get available renderers.
             * @return 
             */
            public List<SRMFRender> getRenderers() {
                return this.renderers;
            }
        }
        
        private final String name; // ID of the render.
        private final String title;
        private final List<SRMFMapRef> references;
        private final List<SRMFMapDestination.SRMFMergeSet> mergeRefSets;

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
            this.mergeRefSets = new ArrayList<SRMFMapDestination.SRMFMergeSet>();
        }

        /**
         * Add a merger set.
         * @param mergeSet 
         */
        protected void addMergeSet(SRMFMapDestination.SRMFMergeSet mergeSet) {
            this.mergeRefSets.add(mergeSet);
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

        /**
         * Get merge sets.
         * 
         * @return 
         */
        public List<SRMFMergeSet> getMergeSets() {
            return Collections.unmodifiableList(this.mergeRefSets);
        }
        
        
    }


    /**
     * Render map constructor.
     * 
     * @param renderers 
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
     * @param providers 
     * @throws java.io.IOException 
     * @throws javax.xml.parsers.ParserConfigurationException 
     * @throws org.xml.sax.SAXException 
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
        
        // Load mapping
        this.rmap.clear();
        NodeList destinationNodeList = mapping.getElementsByTagName("destination");
        for (int destNodeIdx = 0; destNodeIdx < destinationNodeList.getLength(); destNodeIdx++) {
            Element destElement = (Element) destinationNodeList.item(destNodeIdx);

            NodeList mergeNodeList = destElement.getElementsByTagName("merge");
            if (mergeNodeList != null && mergeNodeList.getLength() > 0) {
                // Get merge sets with their references (these would require merging at the end)
                SRMFMapDestination destination = new SRMFMapDestination(destElement.getAttribute("name"),
                                                                        destElement.getAttribute("title"));
                for (int mdlIdx = 0; mdlIdx < mergeNodeList.getLength(); mdlIdx++) {
                    Element mergeNode = (Element) mergeNodeList.item(mdlIdx);
                    SRMFMapDestination.SRMFMergeSet mergeSet = new SRMFMapDestination.SRMFMergeSet(mergeNode.getAttribute("id"),
                                                                                                   mergeNode.getAttribute("out"));
                    NodeList refNodeList = mergeNode.getElementsByTagName("ref");
                    for (int rnlIdx = 0; rnlIdx < refNodeList.getLength(); rnlIdx++) {
                        mergeSet.addReference(this.getSingleReference((Element) refNodeList.item(rnlIdx)));
                    }
                    destination.addMergeSet(mergeSet);
                }
                this.rmap.add(destination);
            } else {
                // Get single references
                SRMFMapDestination destination = new SRMFMapDestination(destElement.getAttribute("name"),
                                                                        destElement.getAttribute("title"));
                this.fillReferences(destElement, destination);
                this.rmap.add(destination);
            }
        }
    }
    
    /**
     * Fill-in the destination references.
     * 
     * @param node
     * @param destination
     * @return 
     */
    private SRMFMapDestination fillReferences(Element node, SRMFMapDestination destination) {
        NodeList refNodeList = node.getElementsByTagName("ref");
        for (int refIdx = 0; refIdx < refNodeList.getLength(); refIdx++) {
            destination.addReference(this.getSingleReference((Element) refNodeList.item(refIdx)));
        }
        
        return destination;
    }
    

    /**
     * Get one reference from the "ref" element.
     * 
     * @param ref
     * @return 
     */
    private SRMFMapDestination.SRMFMapRef getSingleReference(Element ref) {
        NodeList renderNodeList = ref.getElementsByTagName("render");
        SRMFMapDestination.SRMFMapRef reference = new SRMFMapDestination.SRMFMapRef(ref.getAttribute("id"));
        for (int k = 0; k < renderNodeList.getLength(); k++) {
            Element renderElement = (Element) renderNodeList.item(k);
            reference.addRender(new SRMFMapDestination.SRMFMapRef.SRMFRender(renderElement.getAttribute("id"), renderElement.getAttribute("out")));
        }
        
        return reference;
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
     * @param destinationId
     * @return 
     * @throws java.io.IOException
     */
    public File getRenderingStyle(String destinationId) throws IOException {
        File style = new File(this.renderers.getAbsolutePath() + "/" + destinationId + ".xsl");
        if (!style.canRead()) {
            throw new IOException(String.format("File %s is not accessible.", style.getAbsolutePath()));
        }

        return style;
    }
}
