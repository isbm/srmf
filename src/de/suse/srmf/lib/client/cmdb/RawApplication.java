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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author bo
 */
public class RawApplication {
    public static class ConfigurationURL {
        public static final String TYPE_CONFIGURATION = "configuration";
        public static final String TYPE_DATA = "data";
        public static final String TYPE_LOG = "log";

        private final URL url;
        private String type;
        
        /**
         * Configuration URL.
         * 
         * @param url
         * @throws MalformedURLException 
         */
        public ConfigurationURL(String url, String type) throws MalformedURLException {
            this.url = new URL(url);
            if (type.equals(RawApplication.ConfigurationURL.TYPE_CONFIGURATION) ||
                type.equals(RawApplication.ConfigurationURL.TYPE_DATA) ||
                type.equals(RawApplication.ConfigurationURL.TYPE_LOG)) {
                this.type = type;
            } else {
                
            }
        }

        /**
         * Get configuration URL type.
         * 
         * @return 
         */
        public String getType() {
            return type;
        }

        /**
         * Get configuration URL.
         * 
         * @return 
         */
        public URL getUrl() {
            return url;
        }
    }

    private String id;
    private List<EntityOwner> owners; // At least one owner of the app.
    private List<RawApplication.ConfigurationURL> configLocations;
    private List<RawApplication.ConfigurationURL> dataLocations;
    private List<RawApplication.ConfigurationURL> logLocations;
    private String description;

    /**
     * Application ID.
     * 
     * @param id 
     */
    public RawApplication(String id) {
        this.id = id;
        this.owners = new ArrayList<EntityOwner>();
        this.configLocations = new ArrayList<RawApplication.ConfigurationURL>();
        this.dataLocations = new ArrayList<RawApplication.ConfigurationURL>();
        this.logLocations = new ArrayList<RawApplication.ConfigurationURL>();
    }


    /**
     * Set an application description.
     * 
     * @param description 
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * Get description of an application.
     * 
     * @return 
     */
    public String getDescription() {
        return this.description;
    }


    /**
     * Get Application ID.
     * 
     * @return 
     */
    public String getId() {
        return this.id;
    }


    /**
     * Get configuration locations.
     * 
     * @return 
     */
    public List<ConfigurationURL> getConfigLocations() {
        return Collections.unmodifiableList(this.configLocations);
    }

    /**
     * Get data locations.
     * 
     * @return 
     */
    public List<ConfigurationURL> getDataLocations() {
        return Collections.unmodifiableList(this.dataLocations);
    }


    /**
     * Get log locations.
     * 
     * @return 
     */
    public List<ConfigurationURL> getLogLocations() {
        return Collections.unmodifiableList(this.logLocations);
    }
    

    /**
     * Get list of app owners.
     * 
     * @return 
     */
    public List<EntityOwner> getOwners() {
        return Collections.unmodifiableList(this.owners);
    }
    

    /**
     * Add an app owner.
     * 
     * @param owner 
     */
    public void addOwner(EntityOwner owner) {
        this.owners.add(owner);
    }
    

    /**
     * Add configuration URL.
     * 
     * @param configurationURL 
     */
    public void addConfigurationURL(ConfigurationURL configurationURL) {
        this.configLocations.add(configurationURL);
    }
    
    
    /**
     * Add log URL.
     * 
     * @param logURL 
     */
    public void addLogURL(ConfigurationURL logURL) {
        this.logLocations.add(logURL);
    }
    
    
    /**
     * Add data URL.
     * 
     * @param dataURL 
     */
    public void addDataURL(ConfigurationURL dataURL) {
        this.dataLocations.add(dataURL);
    }
}
