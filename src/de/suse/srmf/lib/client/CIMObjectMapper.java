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

import java.util.HashMap;
import javax.cim.CIMClass;
import javax.cim.CIMClassProperty;
import javax.cim.CIMObjectPath;
import javax.cim.CIMQualifier;
import javax.wbem.CloseableIterator;
import javax.wbem.WBEMException;
import javax.wbem.client.WBEMClient;

/**
 *
 * @author bo
 */
public class CIMObjectMapper {
    private String namespace;
    private WBEMClient client;

    public CIMObjectMapper(WBEMClient client, String namespace) {
        this.namespace = namespace;
        this.client = client;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    
    /**
     * Get meaning of the property.
     * 
     * @param value
     * @param property 
     */
    private Object getDataMeaning(CIMClassProperty property) {
        CIMQualifier[] qualifiers = property.getQualifiers();
        String[] mapKeys = null;
        String[] mapValues = null;
        for (int j = 0; j < qualifiers.length; j++) {
            CIMQualifier q = qualifiers[j];
            if (q.getName().toLowerCase().equals("values")) {
                mapValues = (String[]) q.getValue();
            } else if (q.getName().toLowerCase().equals("valuemap")) {
                mapKeys = (String[]) q.getValue();
            }
        }

        HashMap meaningMap = new HashMap();
        if (mapKeys != null && mapValues != null) {
            if (mapKeys.length == mapValues.length) {
                for (int i = 0; i < mapKeys.length; i++) {
                    meaningMap.put(mapKeys[i], mapValues[i]);
                }
            } else {
                System.err.println("Unable to extract map from the description: amount of keys is not amout of values.");
            }
        }
        
        if (property.getValue() != null) {
            return meaningMap.get(property.getValue().toString());
        } else {
            return "N/A";
        }
    }


    /**
     * Explain a class.
     * 
     * @param className
     * @throws WBEMException 
     */
    public void explainClass(String className) throws WBEMException {
        CIMObjectPath path = new CIMObjectPath(null, null, null, this.namespace, className, null);
        System.err.println("Path: " + path + ", namespace: " + this.namespace);
        CloseableIterator<CIMClass> classes = this.client.enumerateClasses(path, true, true, true, true);
        while (classes.hasNext()) {
            CIMClass cls = classes.next();
            System.err.println(cls.getName() + " - " + cls.getPropertyCount() + " properties, parent: " + cls.getSuperClassName());
            CIMClassProperty[] cIMClassPropertys = cls.getProperties();
            System.err.println("    Status:");
            for (int i = 0; i < cIMClassPropertys.length; i++) {
                CIMClassProperty prop = cIMClassPropertys[i];
                if (prop.getValue() != null) { // show all
                    System.err.println("        " + prop.getName() + " is " + this.getDataMeaning(prop));
                }
            }
        }
    }
}
