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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

/**
 *
 * @author bo
 */
public class SRMFLocalStorage implements SRMFStorage {
    public static final String DEFAULT_STORAGE_PATH = "/var/opt/srmf/manifest";
    private File path;
    private boolean compression;

    
    /**
     * Constructor.
     * 
     * @param path 
     */
    public SRMFLocalStorage(File path, boolean compression) throws IOException {
        if (path == null) {
            throw  new IOException("Unknown storage path.");
        }

        if (path.exists() && path.canWrite()) {
            this.path = path;
        } else {
            if (!path.exists()) {
                if (!path.mkdirs()) {
                throw new IOException(String.format("Unable to initialize storage at %s", path.getAbsolutePath()));                    
                }
            } else {
                throw new IOException(String.format("Unable to access storage at %s", path.getAbsolutePath()));
            }
        }
        
        this.compression = compression;
    }
    
    
    @Override
    public void storeMessage(SRMFMessage message) {
        File outfh = new File(this.path.getAbsolutePath() + "/" + message.getObjectId().toLowerCase() + ".lmx"); // .lmx for compressed
        try {
            new FileOutputStream(outfh).write(this.getMessageSource(message));
            System.err.println("> Written " + outfh.getAbsolutePath());
        } catch (Exception ex) {
            Logger.getLogger(SRMFLocalStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    


    /**
     * Get message source as byte array.
     * Depends on settings, it could be compressed.
     * 
     * @param message
     * @return
     * @throws TransformerConfigurationException
     * @throws TransformerException
     * @throws IOException 
     */
    private byte[] getMessageSource(SRMFMessage message)
            throws TransformerConfigurationException,
                   TransformerException,
                   IOException {
        byte[] msg = SRMFUtils.xproc(message.getDocument(), null).getBytes();
        if (this.compression) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream zos = new GZIPOutputStream(baos);
            zos.write(msg);
            zos.close();
            msg = baos.toByteArray();
        }
        
        return msg;
    }


    @Override
    public SRMFMessageDiff diffMessage(SRMFMessage message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
