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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author bo
 */
public class PackagedApplication {
    private String id;
    private final List<EntityOwner> owners; // At least one owner.
    private final List<String> packages;


    /**
     * Constructor.
     */
    public PackagedApplication(String id) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new Exception("Application ID cannot be empty.");
        }
        this.id = id;
        this.packages = new ArrayList<String>();
        this.owners = new ArrayList<EntityOwner>();
    }

    
    /**
     * Get app ID.
     * 
     * @return 
     */
    public String getId() {
        return id;
    }
    

    /**
     * Add owner.
     * 
     * @param owner 
     */
    public void addOwner(EntityOwner owner) {
        this.owners.add(owner);
    }


    /**
     * Add package.
     * 
     * @param packageId 
     */
    public void addPackage(String packageId) {
        this.packages.add(packageId);
    }
    

    /**
     * Get owners of the application.
     * 
     * @return 
     */
    public List<EntityOwner> getOwners() {
        return Collections.unmodifiableList(this.owners);
    }


    /**
     * Get packages.
     * 
     * @return 
     */
    public List<String> getPackages() {
        return Collections.unmodifiableList(this.packages);
    }
}




