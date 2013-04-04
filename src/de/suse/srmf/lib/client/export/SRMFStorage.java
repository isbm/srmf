/*
 */
package de.suse.srmf.lib.client.export;

/**
 * Exporter.
 * 
 * @author bo
 */
public interface SRMFStorage {
    public static final int LOCAL_STORAGE = 0;
    public static final int REMOTE_STORAGE = 1;
    
    public void storeMessage(SRMFMessage message);
    public SRMFMessageDiff diffMessage(SRMFMessage message);
}
