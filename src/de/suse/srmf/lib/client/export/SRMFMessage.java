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
