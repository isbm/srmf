/**
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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author bo
 */
public class SRMFUtils {
    
    /**
     * Get XML document from file.
     * 
     * @param docfile
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException 
     */
    public static Document getXMLDocumentFromFile(File docfile)
            throws ParserConfigurationException,
                   SAXException,
                   IOException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(docfile);
    }

    
    /**
     * Get XML document from string.
     * 
     * @param docSource
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException 
     */
    public static Document getXMLDocumentFromString(String docSource)
            throws ParserConfigurationException,
                   SAXException,
                   IOException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(docSource)));
    }


    /**
     * Get XML out of DOM into the string or transform with XSLT.
     * 
     * @param source
     * @return
     * @throws TransformerConfigurationException
     * @throws TransformerException 
     */
    public static String xproc(Document source, File xsltFile)
            throws TransformerConfigurationException,
                   TransformerException {
        Transformer transformer = null;
        if (xsltFile != null) {
            transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(xsltFile));
        } else {
            transformer = TransformerFactory.newInstance().newTransformer();
        }

        StringWriter out = new StringWriter();
        transformer.transform(new DOMSource(source), new StreamResult(out));
 
        return xsltFile == null ? out.getBuffer().toString().replaceAll("\n|\r", "") : out.getBuffer().toString();
    }

    
    /**
     * Check CLI params.
     * 
     * @param params
     * @param allof
     * @param anyof
     * @throws Exception 
     */
    public static void checkParams(Map<String, String[]> params, String[] allof, String[] anyof) throws Exception {
        for (int i = 0; i < allof.length; i++) {
            if (!params.keySet().contains(allof[i])) {
                throw new Exception(String.format("Parameter \"%s\" is missing.", allof[i]));
            }
        }
        boolean anyofPresent = false;
        for (int i = 0; i < anyof.length; i++) {
            if (params.keySet().contains(anyof[i])) {
                anyofPresent = true;
                break;
            }
        }
        if (!anyofPresent) {
            throw new Exception("I have no idea what to do. See --help for details.");
        }
    }


    /**
     * Get CLI arguments.
     *
     * @param args
     * @param singleArgs
     * @return
     * @throws Exception
     */
    public static Map<String, String[]> getArgs(String[] args, String... singleArgs) throws Exception {
        Map<String, String[]> params = new HashMap<String, String[]>();
        for (int i = 0; i < args.length; i++) {
            String inArg = args[i];
            if (!inArg.startsWith("--")) {
                throw new Exception("Wrong argument syntax: " + inArg);
            } else {
                inArg = inArg.substring(2);
            }
            for (int j = 0; j < singleArgs.length; j++) {
                if (singleArgs[j].equals(inArg)) {
                    params.put(inArg, new String[]{});
                }
            }
            if (inArg.contains("=")) {
                String[] keyset = inArg.split("=", 2);
                if (keyset[1].startsWith("{") && keyset[1].endsWith("}")) {
                    params.put(keyset[0], new String[]{keyset[1].substring(1, keyset[1].length() - 1)});
                } else {
                    params.put(keyset[0], keyset[1].split(","));
                }
            }
        }
        return params;
    }
}
