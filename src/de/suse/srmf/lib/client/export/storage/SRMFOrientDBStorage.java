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

package de.suse.srmf.lib.client.export.storage;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import de.suse.srmf.lib.client.export.SRMFMessage;
import de.suse.srmf.lib.client.export.SRMFMessageDiff;
import de.suse.srmf.lib.client.export.SRMFUtils;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;




/**
 * Parses and stores the CIM object.
 * 
 * @author bo
 */
class SimpleCIMObject {    
    private Document doc;

    public SimpleCIMObject() {}
    
    private ODocument cimSetNamespacePath(ODocument vertex, Node instance) {
        NodeList namespacePathList = ((Element) instance).getElementsByTagName("NAMESPACEPATH");
        for (int i = 0; i < namespacePathList.getLength(); i++) {
            Node el = namespacePathList.item(i);
            // Get host name
            NodeList nl = ((Element) el).getElementsByTagName("HOST");
            if (nl.getLength() > 0) {
                vertex.field("HOST", SRMFUtils.getDOMElementText(nl.item(0), null, null).toString());
            }
            
            // Get local name spacepath
            nl = ((Element) el).getElementsByTagName("LOCALNAMESPACEPATH");
            StringBuilder namespace = new StringBuilder();
            if (nl.getLength() > 0) {
                nl = ((Element) nl.item(0)).getElementsByTagName("NAMESPACE");
                for (int nsi = 0; nsi < nl.getLength(); nsi++) {
                    namespace.append(((Element) nl.item(nsi)).getAttribute("NAME"));
                    if (nsi < nl.getLength() - 1) {
                        namespace.append("/");
                    }
                }
            }

            if (!namespace.toString().isEmpty()) {
                vertex.field("LOCALNAMESPACEPATH", namespace.toString());
            }
        }

        return vertex;
    }


    /**
     * Collect all the properties from one instance.
     * 
     * @param vertex
     * @param instance
     * @return 
     */
    private ODocument cimSetProperties(ODocument vertex, Node instance) {
        System.err.println("Setting properties to the instance " + instance);
        NodeList propertyNodeList = ((Element) instance).getElementsByTagName("PROPERTY");
        for (int i = 0; i < propertyNodeList.getLength(); i++) {
            Node propertyNode = propertyNodeList.item(i);
            String pKey = ((Element) propertyNode).getAttribute("NAME").trim();
            String pValue = SRMFUtils.getDOMElementText(propertyNode, null, null).toString().trim();
            if (!pValue.isEmpty()) {
                vertex.field(pKey, pValue);
            }
        }

        return vertex;
    }
    

    /**
     * Get DOM instances.
     * 
     * @return 
     */
    private NodeList getInstances() {
        NodeList instances = this.doc.getElementsByTagName("VALUE.NAMEDINSTANCE");
        if (instances.getLength() == 0) {
            instances = this.doc.getElementsByTagName("VALUE.OBJECTWITHPATH");
        }

        return instances;
    }
    
    /**
     * Get one CIM instance.
     * 
     * @param instance 
     */
    private void cimCreateInstanceDoc(Node instance) throws IOException {
        OrientConnection db = OrientDBConnectionPool.getInstance().acquire();
        ODocument obj = db.newInstance(((Element)((Element) instance).getElementsByTagName("INSTANCENAME").item(0)).getAttribute("CLASSNAME"));
        this.cimSetNamespacePath(obj, instance);
        this.cimSetProperties(obj, instance);
        obj.save();
        ORID oid = obj.getIdentity();
        db.close();
    }

    /**
     * Store the document to the OrientDB.
     * 
     * @param document 
     */
    public void store(Document document) throws IOException {
        this.doc = document;
        NodeList instances = this.getInstances();
        for (int i = 0; i < instances.getLength(); i++) {
            Node instance = instances.item(i);
            this.cimCreateInstanceDoc(instance);
        }        
    }

    private void dumpNodeTree(Node node, int offset) {
        String shift = "";
        for (int i = 0; i < offset; i++) {
            shift = shift + "  ";
        }
        
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node n = node.getChildNodes().item(i);
            if (n.getNodeType() != Document.TEXT_NODE) {
                System.err.println(String.format("%s%s", shift, n.getNodeName()));
                this.dumpNodeTree(n, offset + 1);
            }
        }
    }
}


/**
 *
 * @author bo
 */
public class SRMFOrientDBStorage implements SRMFStorage {
    // Config
    public static final String DB_URL = ".srmf.graph.url";
    public static final String DB_USER = ".srmf.graph.user";
    public static final String DB_PASSWORD = ".srmf.graph.password";
    
    private static SRMFOrientDBStorage instance;
    

    /**
     * Exception, related to the SRMF OrientDB storage init error.
     */
    public static class SRMFOrientStorageException extends Exception {
        private SRMFOrientStorageException(String message) {
            super(message);
        }
    }


    private SRMFOrientDBStorage()
            throws SRMFOrientStorageException,
                   IOException {
    }


    /**
     * Instance singleton. This one registers the factory pool only once to the Orient DB.
     * @return 
     */
    public static synchronized SRMFOrientDBStorage getInstance() 
            throws SRMFOrientStorageException,
                   IOException {
        if (SRMFOrientDBStorage.instance == null) {
            SRMFOrientDBStorage.instance = new SRMFOrientDBStorage();
        }

        return SRMFOrientDBStorage.instance;
    }
    

    @Override
    public void storeMessage(SRMFMessage message) {
        try {
            new SimpleCIMObject().store(message.getDocument());
        } catch (IOException ex) {
            System.err.println("Error storing message: " + ex.getLocalizedMessage());
        }
    }
    
    
    /**
     * Get CIM property.
     * 
     * @param property
     * @return 
     */
    private SimpleEntry getCIMProperty(Element property) {
        if (!property.hasAttribute("NAME")) {
            return null;
        }

        NodeList values = property.getElementsByTagName("VALUE");
        if (values != null && values.getLength() > 0) {
            Node value = values.item(0);
            NodeList contentNodes = value.getChildNodes();
            for (int i = 0; i < contentNodes.getLength(); i++) {
                return new SimpleEntry(property.getAttribute("NAME"), contentNodes.item(i).getNodeValue());
            }
        }

        return null;
    }


    @Override
    public SRMFMessageDiff diffMessage(SRMFMessage message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
