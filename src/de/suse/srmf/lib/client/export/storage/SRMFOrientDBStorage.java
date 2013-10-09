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

import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.ODatabaseThreadLocalFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import de.suse.srmf.lib.client.SRMFConfig;
import de.suse.srmf.lib.client.export.SRMFMessage;
import de.suse.srmf.lib.client.export.SRMFMessageDiff;

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

    /**
     * Database connection factory.
     */
    public static class ODBConnectionFactory implements ODatabaseThreadLocalFactory {
        private String url;
        private String user;
        private String password;

        private ODBConnectionFactory(SRMFConfig config) throws SRMFOrientStorageException {
            this.url = config.getItem(SRMFOrientDBStorage.DB_URL, null);
            this.user = config.getItem(SRMFOrientDBStorage.DB_USER, null);
            this.password = config.getItem(SRMFOrientDBStorage.DB_PASSWORD, null);
            
            if (this.url == null) {
                throw new SRMFOrientStorageException("OrientDB URL cannot be empty in the configuration");
            } else if (this.user == null) {
                throw new SRMFOrientStorageException("OrientDB user should be specified in the configuration");
            } else if (this.password == null) {
                throw new SRMFOrientStorageException("OrientDB password credentials should be specified in the configuration");
            }
        }

        @Override
        public ODatabaseRecord getThreadDatabase() {
            return ODatabaseDocumentPool.global().acquire(this.url, this.user, this.password);
        }
    }

    private SRMFOrientDBStorage() throws SRMFOrientStorageException {
        Orient.instance().registerThreadDatabaseFactory(new ODBConnectionFactory(null));
    }

    /**
     * Instance singleton. This one registers the factory pool only once to the Orient DB.
     * @return 
     */
    public static synchronized SRMFOrientDBStorage getInstance() {
        if (SRMFOrientDBStorage.instance == null) {
            
        }

        return SRMFOrientDBStorage.instance;
    }
    

    @Override
    public void storeMessage(SRMFMessage message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SRMFMessageDiff diffMessage(SRMFMessage message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
