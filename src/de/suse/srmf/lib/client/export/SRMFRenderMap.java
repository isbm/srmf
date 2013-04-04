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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author bo
 */
public class SRMFRenderMap {
    public static final String DEFAUILT_SRMF_RENDER_PATH = "/etc/srmf/renderers";
    private List<SRMFMapDestination> rmap;
    private final File renderers;

    /**
     * Map destinations
     */
    public static class SRMFMapDestination {
        /**
         * Map rendering reference
         */
        public static class SRMFMapRef {
            private String namespace;
            private String base; // Root base class threshold. Allows to specify higher levels with other render.
            private String id;

            public SRMFMapRef(String namespace, String base, String id) {
                this.namespace = namespace;
                this.base = base;
                this.id = id;
            }

            public String getNamespace() {
                return namespace;
            }

            public String getBase() {
                return base;
            }

            public String getId() {
                return id;
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
     * Render map.
     * 
     * @param renderMapXMLSource 
     */
    public SRMFRenderMap(File renderers) {
        this.renderers = renderers;
        this.rmap = new ArrayList<SRMFMapDestination>();
    }
    
    /**
     * Load mapping from the file.
     * 
     * @param mapping 
     */
    public void loadFromFile(File mapping)
            throws IOException,
                   ParserConfigurationException,
                   SAXException {
        this.init(SRMFUtils.getXMLDocumentFromFile(mapping));
    }


    /**
     * Init the mapping.
     * 
     * @param doc 
     */
    private void init(Document doc) {
        this.rmap.clear();
        NodeList destinationNodeList = doc.getElementsByTagName("destination");
        for (int i = 0; i < destinationNodeList.getLength(); i++) {
            Element destElement = (Element) destinationNodeList.item(i);
            SRMFMapDestination destination = new SRMFMapDestination(destElement.getAttribute("name"),
                                                                    destElement.getAttribute("title"));
            NodeList refNodeList = destElement.getElementsByTagName("ref");
            for (int j = 0; j < refNodeList.getLength(); j++) {
                Element refElement = (Element) refNodeList.item(j);
                destination.addReference(new SRMFMapDestination.SRMFMapRef(refElement.getAttribute("namespace"),
                                                                           refElement.getAttribute("base"), 
                                                                           refElement.getAttribute("id")));
            }
            this.rmap.add(destination);
        }
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
