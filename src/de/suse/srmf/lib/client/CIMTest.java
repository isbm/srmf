/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.suse.srmf.lib.client;

import java.util.Locale;
import javax.cim.CIMObjectPath;
import javax.security.auth.Subject;
import javax.wbem.CloseableIterator;
import javax.wbem.client.PasswordCredential;
import javax.wbem.client.UserPrincipal;
import javax.wbem.client.WBEMClient;
import javax.wbem.client.WBEMClientConstants;
import javax.wbem.client.WBEMClientFactory;

/**
 *
 * @author bo
 */
public class CIMTest {
    public static void main(String[] args) throws Exception {
        System.setProperty("sblim.wbem.traceFileLevel", "ALL");
        System.setProperty("java.net.debug", "all");
        WBEMClient client = WBEMClientFactory.getClient(WBEMClientConstants.PROTOCOL_CIMXML);
        CIMObjectPath path = new CIMObjectPath("https", "e104.suse.de", "5989", null, null, null);
        Subject subj = new Subject();
        subj.getPrincipals().add(new UserPrincipal("wsman"));
        subj.getPrivateCredentials().add(new PasswordCredential("secret"));
        client.initialize(path, subj, new Locale[]{Locale.US});
        client.enumerateInstances(new CIMObjectPath(null, null, null, "root/cimv2", "CIM_ComputerSystem", null), true, false, true, null);
    }
}
