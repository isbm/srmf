/*
 */
package de.suse.srmf.lib.client.export;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author bo
 */
public class SRMFMessage {
    private String baseClassName;
    private String className;
    private Document document;

    /**
     * Constructor for the SRMF Message.
     * 
     * @param baseClassName
     * @param className
     * @param source 
     */
    public SRMFMessage(String baseClassName, String className, String source)
            throws ParserConfigurationException,
                   SAXException,
                   IOException {
        this.baseClassName = baseClassName;
        this.className = className;
        this.document = SRMFUtils.getXMLDocumentFromString(source);
    }

    
    /**
     * Get base abstract class of the provider.
     * 
     * @return 
     */
    public String getBaseClassName() {
        return baseClassName;
    }

    
    /**
     * Get class name of the directly corresponding provider.
     * @return 
     */
    public String getClassName() {
        return this.className;
    }


    /**
     * Get DOM document.
     * 
     * @return 
     */
    public Document getDocument() {
        return this.document;
    }
    
    
    /**
     * Get XML source.
     * 
     * @return 
     */
    public String getXMLSource() {
        return null;
    }

    /**
     * 
     * @param mapping
     * @return 
     */
    public String transform(SRMFRenderMap mapping) {
        return null;
    }
}
