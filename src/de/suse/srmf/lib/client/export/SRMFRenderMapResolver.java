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

import de.suse.srmf.lib.client.export.SRMFRenderMap.SRMFMapProvider;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Rendering map resolver. Recursively resolve all includes in the templates.
 * 
 * @author bo
 */
public final class SRMFRenderMapResolver {
    public static final String SRMF_MANIFEST_PATH = "/etc/srmf/manifest/";
    private Map<String, SRMFRenderMap.SRMFMapProvider> providers;

    /**
     * Constructor. Takes an URL of an optional index.
     * If null, /etc/srmf/manifest/info/srmf-index.xml is taken.
     * 
     * @param index 
     */
    public SRMFRenderMapResolver(URL index)
            throws MalformedURLException,
                   Exception {
        if (index == null) {
            index = new URL(String.format("file://%s/info/srmf-index.xml",
                                          SRMFRenderMapResolver.SRMF_MANIFEST_PATH));
        }
        
        if (!index.getProtocol().equals("file")) {
            throw new Exception("Protocol %s is not supported now.".format(index.getProtocol()));
        }
        this.providers = new HashMap<String, SRMFMapProvider>();
        this.parse(index);
    }

    
    /**
     * Recursively parse all the templates.
     * 
     * @param index 
     */
    private void parse(URL index)
            throws Exception {
        Document doc = SRMFUtils.getXMLDocumentFromFile(new File(index.toURI()));
        NodeList objectsNodeList = doc.getElementsByTagName("objects");
        if (objectsNodeList == null || objectsNodeList.getLength() != 1) {
            throw new Exception("Source %s is invalid.".format(index.toString()));
        }
        
        NodeList elements = ((Element) objectsNodeList.item(0)).getChildNodes();
        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);
            if (node.getNodeName().equals("include")) {
                this.parse(new URL(((Element) node).getAttribute("url")));
            } else if (node.getNodeName().equals("object")) {
                Element objElement = (Element) elements.item(i);
                SRMFMapProvider provider = new SRMFMapProvider(objElement.getAttribute("path"), 
                                                               objElement.getAttribute("id"),
                                                               objElement.getAttribute("title"),
                                                               this.getQuery(objElement));
                this.providers.put(provider.getId(), provider);
            }
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
     * Get resolved providers.
     * @return 
     */
    public Map<String, SRMFMapProvider> getProviders() {
        return Collections.unmodifiableMap(this.providers);
    }
}
