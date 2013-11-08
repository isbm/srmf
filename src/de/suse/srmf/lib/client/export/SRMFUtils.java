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
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



/**
 * Prints ASCII table.
 * 
 * @author bo
 */
class CLITablePrint {
    private List<List> table;
    private int [] widths;

    /**
     * Constructor.
     * 
     * @param table 
     */
    public CLITablePrint(List<List> table) {
        this.table = table;
    }

    /**
     * Check if the table is consistent grid.
     * Header is a leader line.
     */
    private void check() throws Exception {
        if (this.table == null) {
            this.table = new ArrayList<List>();
        }
        
        if (this.table.isEmpty()) {
            throw new Exception("Table cannot be empty!");
        }
        
        int header = 0;
        for (int i = 0; i < table.size(); i++) {
            header = table.get(i).size();
            if (table.get(i).size() != header) {
                throw new Exception("Data in table is not equally distributed!");
            }
        }
    }


    /**
     * Find extra-widths by my width of any value.
     */
    private void getWidths() {
        this.widths = new int[this.table.get(0).size()];
        int cellLen = 0;
        for (int i = 0; i < this.table.size(); i++) {
            List row = this.table.get(i);
            for (int idx = 0; idx < row.size(); idx++) {
                cellLen = row.get(idx).toString().length();
                if (cellLen > this.widths[idx]) {
                    this.widths[idx] = cellLen;
                }
            }
        }
    }


    /**
     * Format the table render on the CLI.
     * 
     * @return 
     */
    private String format() {
        StringBuilder out = new StringBuilder();
        List<List> ftable = new ArrayList<List>();
        for (int i = 0; i < this.table.size(); i++) {
            List row = this.table.get(i);
            List<String> frow = new ArrayList<String>();
            for (int idx = 0; idx < widths.length; idx++) {
                String data = row.get(idx) + this.makeWS(this.widths[idx] - row.get(idx).toString().length(), " ");
                frow.add(data);
            }
            ftable.add(frow);
        }

        List<String> strips = new ArrayList<String>();
        for (int idx = 0; idx < ftable.size(); idx++) {
            strips.clear();
            List<String> row = ftable.get(idx);
            for (int i = 0; i < row.size(); i++) {
                strips.add(this.makeWS(row.get(i).length(), idx == 1 ? "=" : "-"));
            }
        
            out.append(this.join(idx == 1 ? "===" : "-+-", strips)).append("\n").append(this.join(" | ", row)).append("\n");
        }

        out.append(this.join("-+-", strips)).append("\n");

        return out.toString();
    }


    @Override
    public String toString() {
        try {
            this.check();
            this.getWidths();
            return this.format();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "#ERROR: " + ex.getLocalizedMessage();
        }
    }
    
    /**
     * Equivalent to "foo".join(array) in Python.
     * 
     * @param delimeter
     * @param data
     * @return 
     */
    private String join(String delimiter, Collection<?> data) {
        StringBuilder out = new StringBuilder();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            out.append(iter.next());
            if (!iter.hasNext()) {
                break;
            }
            out.append(delimiter);
        }

        return out.toString();
    }
    

    private String makeWS(int times, String wsc) {
        if (wsc == null) {
            wsc = " ";
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < times; i++) {
            out.append(wsc);
        }

        return out.toString();
    }
}

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
    
    
    public static void printTable(List<List> table, PrintStream stream) {
        if (stream == null) {
            stream = System.out;
        }

        stream.println(new CLITablePrint(table));
    }
    
    /**
     * Extracts "foo" from <bar>foo</bar> node.
     * Concatenates all the text in child nodes, i.e. produces "foobar"
     * from <bar>foo<foo>bar</foo></bar>.
     * 
     * @param node
     * @return 
     */
    public static StringBuilder getDOMElementText(Node node, StringBuilder buffer, String separator) {
        if (buffer == null) {
            buffer = new StringBuilder();
        }
        
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node contentNode = children.item(i);
            if (contentNode.getNodeType() == Document.TEXT_NODE || 
                contentNode.getNodeType() == Document.CDATA_SECTION_NODE) {
                buffer.append(contentNode.getNodeValue());
                if (separator != null) {
                    buffer.append(separator);
                }
            } else {
                buffer = SRMFUtils.getDOMElementText(contentNode, buffer, separator);
            }
        }

        return buffer;
    }
}
