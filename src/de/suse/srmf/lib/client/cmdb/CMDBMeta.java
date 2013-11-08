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

package de.suse.srmf.lib.client.cmdb;

import de.suse.srmf.lib.client.export.SRMFUtils;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * CMDB Meta is to store a primary description about particular node
 * and the running software on it. This is used by Application Management (ITIL v3).
 * 
 * @author bo
 */
public class CMDBMeta {
    public static final String SRMF_CMDB_META = "file:///etc/srmf/cmdb.map";

    private Document meta;
    private final URL srmfCMDBMetaUrl;

    public CMDBMeta()
            throws MalformedURLException,
                   URISyntaxException,
                   Exception {
        this.srmfCMDBMetaUrl = new URL(CMDBMeta.SRMF_CMDB_META);
        File handle = new File(this.srmfCMDBMetaUrl.toURI());
        if (!handle.exists() || !handle.canRead()) {
            throw new Exception(String.format("File %s does not exists or is not accessible.",
                                              this.srmfCMDBMetaUrl.getPath()));
        }
        
        this.meta = SRMFUtils.getXMLDocumentFromFile(handle);
    }
    
    /**
     * Get system owner.
     * @return
     */
    public SystemOwner getSystemOwner() throws Exception {
        SystemOwner owner = new SystemOwner();
        Element node = this.getComputerSystemNode();
        owner.setFullName(SRMFUtils.getDOMElementText(node.getElementsByTagName("name").item(0), null, null).toString());
        owner.setMemo(SRMFUtils.getDOMElementText(node.getElementsByTagName("memo").item(0), null, null).toString());
        owner.getContact().setEmail(SRMFUtils.getDOMElementText(node.getElementsByTagName("email").item(0), null, null).toString());

        return owner;
    }


    private Element getComputerSystemNode() throws Exception {
        NodeList nodes = this.meta.getElementsByTagName("node");
        if (nodes != null && nodes.getLength() > 0) {
            return (Element) nodes.item(0);
        }
        
        throw new Exception("CMDB Meta has unrecognitable structure.");
    }


    /**
     * Save data of the system owner.
     * @param owner 
     */
    public void setSystemOwner(SystemOwner owner) {
    }
}
