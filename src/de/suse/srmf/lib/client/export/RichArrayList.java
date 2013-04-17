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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author bo
 */
public class RichArrayList<T> extends ArrayList {
    private Map<String, Object> attributes;

    public RichArrayList() {
        super();
    }

    public RichArrayList(Collection collection) {
        super(collection);
    }

    public RichArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Set an attribute to the list.
     * 
     * @param id
     * @param obj 
     */
    public void addAttribute(String id, Object obj) {
        this.getAttributes().put(id, obj);
    }
    
    /**
     * Get attributes from the list.
     * 
     * @param id
     * @return 
     */
    public Object getAttribute(String id) {
        return this.getAttributes().get(id);
    }

    /**
     * Get the attributes mapping lazy way.
     * 
     * @return 
     */
    private synchronized Map<String, Object> getAttributes() {
        if (this.attributes == null) {
            this.attributes = new HashMap<String, Object>();
        }

        return this.attributes;
    }
}
