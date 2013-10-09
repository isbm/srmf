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

package de.suse.srmf.lib.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author bo
 */
public final class SRMFConfig {
    private Properties setup;
    private Map<String, URL> hosts;
    private static SRMFConfig instance;

    /**
     * Constructor of the config bean.
     * 
     * @param alternativePath
     * @throws IOException 
     */
    private SRMFConfig(String alternativePath) throws IOException {
        File setupFile = new File(alternativePath != null ? alternativePath : "/etc/srmf.conf");
        if (!setupFile.exists()) {
            System.err.println(String.format("Warning: %s does not exists. Using default in current directly.", setupFile.getCanonicalPath()));
            setupFile = new File("srmf.conf");
        }

        if (!setupFile.exists()) {
            System.err.println(String.format("Error: file %s does not exists!", setupFile.getAbsolutePath()));
            System.exit(0);
        }
        
        this.hosts = new HashMap<String, URL>();
        this.setup = new Properties();
        this.setup.load(new FileInputStream(setupFile));
        
        this.parse();
    }

    /**
     * Get already initialized instance or initialize one without an alternative config path.
     * 
     * @return 
     */
    public static SRMFConfig getInstance() throws IOException {
        if (SRMFConfig.instance == null) {
            return SRMFConfig.initialize(null);
        }

        return SRMFConfig.instance;
    }

    
    /**
     * Initializes the config.
     * 
     * @param alternativePath
     * @return
     * @throws IOException 
     */
    public static synchronized SRMFConfig initialize(String alternativePath) throws IOException {
        if (SRMFConfig.instance == null) {
            SRMFConfig.instance = new SRMFConfig(alternativePath);
        }

        return SRMFConfig.instance;
    }
    

    private void parse() throws MalformedURLException {
        Iterator keys = this.setup.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (!key.startsWith(".srmf")) {
                this.hosts.put(key, new URL(this.setup.getProperty(key)));
            }
        }
    }

    
    private String getUrlCredentials(String id, boolean username) {
        String userInfo = this.hosts.get(id).getUserInfo();
        if (userInfo != null) {
            return userInfo.split(":")[username ? 0 : 1];
        }

        return null;
    }


    /**
     * Get port of the client host.
     * 
     * @param id
     * @return 
     */
    public int getPort(String id) {
        return this.hosts.get(id).getPort();
    }
    
    
    /**
     * Get username in the URL from the configuration.
     * 
     * @param id
     * @return 
     */
    public String getUsername(String id) {
        return this.getUrlCredentials(id, true);
    }


    /**
     * Get password in the URL from the configuration.
     * 
     * @param id
     * @return
     */
    public String getPassword(String id) {
        return this.getUrlCredentials(id, false);
    }
    
    /**
     * Get get the CIM namespace.
     * 
     * @param id
     * @return 
     */
    public String getNamespace(String id) {
        return this.hosts.get(id).getPath();
    }
    
    /**
     * Get hostname.
     * 
     * @param id
     * @return 
     */
    public String getHostname(String id) {
        return this.hosts.get(id).getHost();
    }

    
    /**
     * Get the scheme protocol.
     * 
     * @param id
     * @return 
     */
    public String getScheme(String id) {
        return this.hosts.get(id).getProtocol();
    }

    /**
     * Get connection URL.
     * 
     * @param id
     * @return
     * @throws MalformedURLException 
     */
    public URL getConnectionUrl(String id) throws MalformedURLException {
        return new URL(String.format("%s://%s:%s", this.getScheme(id), this.getHostname(id), this.getPort(id)));
    }
    
    /**
     * Get config item.
     * 
     * @param id
     * @param defaultValue
     * @return 
     */
    public String getItem(String id, String defaultValue) {
        return this.setup.getProperty(id, defaultValue);
    }
}
