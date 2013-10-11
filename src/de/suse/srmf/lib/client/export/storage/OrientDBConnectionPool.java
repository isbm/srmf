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

import de.suse.srmf.lib.client.SRMFConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bo
 */
public class OrientDBConnectionPool {
    
    private List<OrientConnection> pool;
    private static OrientDBConnectionPool instance;

    private OrientDBConnectionPool() throws IOException {
        this.pool = new ArrayList<OrientConnection>();
        for (int i = 0; i < Integer.parseInt(SRMFConfig.getInstance().getItem(".srmf.graph.maxconn", "2")); i++) {
            this.addConnection();
        }
    }
    
    private OrientConnection addConnection() throws IOException {
        OrientConnection conn = new OrientConnection(SRMFConfig.getInstance().getItem(".srmf.graph.url", null),
                                                     SRMFConfig.getInstance().getItem(".srmf.graph.user", null),
                                                     SRMFConfig.getInstance().getItem(".srmf.graph.password", null));
        this.pool.add(conn);
        return conn;
    }

    /**
     * Acquire new connection. If not enough, new will be connected.
     * 
     * @return
     * @throws IOException 
     */
    public OrientConnection acquire() throws IOException {
        this.closeExcessedConnections();
        for (int i = 0; i < this.pool.size(); i++) {
            OrientConnection orientConnection = pool.get(i);
            if (!orientConnection.isClosed()) {
                return orientConnection;
            }
        }
        
        return this.addConnection();
    }
    
    /**
     * Rips off unnesessary connections away.
     */
    private void closeExcessedConnections() {
        // XXX: Implement exceded connection closing.
    }
    

    /**
     * Get pool instance.
     * 
     * @return
     * @throws IOException 
     */
    public static synchronized OrientDBConnectionPool getInstance() throws IOException {
        if (OrientDBConnectionPool.instance == null) {
            OrientDBConnectionPool.instance = new OrientDBConnectionPool();
        }

        return OrientDBConnectionPool.instance;
    }
}
