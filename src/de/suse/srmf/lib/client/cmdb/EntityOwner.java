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

/**
 *
 * @author bo
 */
public class EntityOwner {
    
    /**
     * Contact block.
     */
    public static class Contact {
        private String email;
        public Contact() {}

        /**
         * Set contact's email.
         * @param email 
         */
        public void setEmail(String email) {
            this.email = email != null ? email.trim() : "";
        }

        /**
         * Get contact's email.
         * @return 
         */
        public String getEmail() {
            return email;
        }
    }

    private String ownerName;
    private EntityOwner.Contact contact;
    private String memo;
    private String description;

    /**
     * Constructor.
     */
    public EntityOwner() {
        this.contact = new Contact();
    }

    public Contact getContact() {
        return contact;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    public String getDescription() {
        return description;
    }

    public void setMemo(String memo) {
        this.memo = memo != null ? memo.trim() : "";
    }

    public String getMemo() {
        return memo;
    }

    public void setFullName(String ownerName) {
        this.ownerName = ownerName != null ? ownerName.trim() : "";
    }

    public String getFullName() {
        return ownerName;
    }
}
