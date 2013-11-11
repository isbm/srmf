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

import com.hazelcast.instance.TerminatedLifecycleService;
import de.suse.srmf.lib.client.export.SRMFUtils;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author bo
 */
public class CMDBOperations {
    public static final String CMDB_INFO_OWNER = "owner";
    public static final String CMDB_INFO_ALLAPPS = "allapps";
    public static final String CMDB_INFO_PKGAPPS = "pkgapps";
    public static final String CMDB_INFO_RAWAPPS = "rawapps";

    private final CMDBMeta meta;

    public CMDBOperations()
            throws URISyntaxException,
                   Exception {
        this.meta = new CMDBMeta();
    }


    /**
     * Get owner information.
     * @throws java.lang.Exception
     */
    private void getOwnerInformation(EntityOwner owner) throws Exception {
        if (owner != null) {
            System.err.println("\n\tOwner:");
            System.err.println(String.format("\t\tName:    %s", owner.getFullName()));
            System.err.println(String.format("\t\tContact: %s", owner.getContact().getEmail()));
            System.err.println(String.format("\t\tMemo:    %s", owner.getMemo()));
            System.err.println(String.format("\t\tInfo:    %s", owner.getDescription()));
        } else {
            System.err.println("Error:\n\tData about system owner was not found. Please set one.");
        }
    }
    
    private void getPackagedApplicationsInformation() throws Exception {
        List<PackagedApplication> packagedApps = this.meta.getPackagedApps();
        if (packagedApps.size() > 0) {
            System.err.println("Packaged applications");
            System.err.println("=====================");
            for (int i = 0; i < packagedApps.size(); i++) {
                PackagedApplication app = packagedApps.get(i);
                // Display app ID
                System.err.println("\n\tApplication: " + app.getId());

                // Display owners
                List<EntityOwner> owners = app.getOwners();
                for (int ownerIdx = 0; ownerIdx < owners.size(); ownerIdx++) {
                    this.getOwnerInformation(owners.get(ownerIdx));
                }
                
                // Display packages
                List<String> packages = app.getPackages();
                System.err.println("\tPackages:");
                for (int pkgIdx = 0; pkgIdx < packages.size(); pkgIdx++) {
                    System.err.println("\t\t" + packages.get(pkgIdx));
                }
            }
            System.err.println("\n\n");
        }
    }


    private void printItems(List<RawApplication.ConfigurationURL> items,
                            String hello, String error) {
        System.err.println(String.format("\n\t%s:", hello));
        if (items.size() > 0) {
            for (int cfgIdx = 0; cfgIdx < items.size(); cfgIdx++) {
                RawApplication.ConfigurationURL cfgPath = items.get(cfgIdx);
                System.err.println(String.format("\t\t%s", cfgPath.getUrl().toString()));
            }
        } else {
            System.err.println("\t\t" + error);
        }        
    }


    private void getRawApplicationsInformation() throws Exception {
        List<RawApplication> rawApps = this.meta.getRawApplications();
        if (rawApps.size() > 0) {
            System.err.println("Raw (custom) applications");
            System.err.println("=========================");
            for (int ridx = 0; ridx < rawApps.size(); ridx++) {
                RawApplication app = rawApps.get(ridx);
                // Display app ID
                System.err.println("\n\tApplication: " + app.getId());

                // Display app descr
                System.err.println("\n\tDescription:\n" + SRMFUtils.textWrap(app.getDescription(), 80, "                "));

                // Display owners
                List<EntityOwner> owners = app.getOwners();
                for (int ownerIdx = 0; ownerIdx < owners.size(); ownerIdx++) {
                    this.getOwnerInformation(owners.get(ownerIdx));
                }

                // Get locations
                this.printItems(app.getConfigLocations(), "Configuration locations", "This application has no defined configs.");
                this.printItems(app.getDataLocations(), "Data locations", "This application has no defined data locations.");
                this.printItems(app.getLogLocations(), "Log locations", "This application has no defined locations of the logs.");
            }
            System.err.println("\n\n");
        }
    }

    /**
     * Get CMDB meta information.
     * 
     * @param targets 
     */
    public void doGetInfo(String ... targets) throws Exception {
        boolean accepted = false;
        for (int i = 0; i < targets.length; i++) {
            if (targets[i].equals(CMDBOperations.CMDB_INFO_OWNER)) {
                this.getOwnerInformation(this.meta.getSystemOwner());
                accepted = true;
            } else if (targets[i].equals(CMDBOperations.CMDB_INFO_PKGAPPS)) {
                this.getPackagedApplicationsInformation();
                accepted = true;
            } else if (targets[i].equals(CMDBOperations.CMDB_INFO_RAWAPPS)) {
                this.getRawApplicationsInformation();
                accepted = true;
            } else if (targets[i].equals(CMDBOperations.CMDB_INFO_ALLAPPS)) {
                this.getPackagedApplicationsInformation();
                this.getRawApplicationsInformation();
                accepted = true;
            }
        }
        
        if (!accepted) {
            throw new Exception("Error: no known targets has been passed.");
        }
    }


    /**
     * Write owner info.
     * 
     * @throws Exception 
     */
    public void doInteractive() throws Exception {
        final Map result = new HashMap();
        InteractiveConsole console = new InteractiveConsole("CMDB> ");
        console.addConsoleCallback(new InteractiveConsole.ConsoleCallback() {
            private Boolean terminated = false;
            private Map setup;
            
            private Map getSetup() {
                if (this.setup == null) {
                    this.setup = new HashMap();
                    this.setup.put("node/owner/name", "Name of the owner of the current node.");
                    this.setup.put("node/owner/description", "Description of the owner.");
                    this.setup.put("node/owner/memo", "Short memo, related to the owner.");
                    this.setup.put("node/owner/contact/email", "Email address how to contact the owner.");
                }

                return this.setup;
            }
            
            private SimpleEntry parseCommand(String command) {
                String[] tokens = command != null ? command.replaceAll("\\s+", " ").split(" ", 3) : new String[]{};
                if (tokens.length == 3 && tokens[0].equals("set")) {
                    if (this.getSetup().get(tokens[1]) != null) {
                        return new SimpleEntry(tokens[1], tokens[2]);
                    } else {
                        System.err.println("Unknown target " + tokens[1]);
                    }
                } else if (tokens.length > 0 && tokens[0].equals("help")) {
                    System.err.println("Available targets:");
                    Iterator keyset = this.getSetup().keySet().iterator();
                    while (keyset.hasNext()) {
                        String k = (String) keyset.next();
                        System.err.println("\t" + k + "\t" + this.getSetup().get(k));
                    }
                    System.err.println("");
                } else if (tokens.length > 0 && tokens[0].equals("save")) {
                    return new SimpleEntry("_saved", "_saved");
                } else if (tokens.length > 0 && tokens[0].equals("done")) {
                    System.err.println("Bye!");
                    this.terminated = Boolean.TRUE;
                } else {
                    System.err.println("Unknown command: " + tokens[0]);
                    System.err.println("Usage: set <key> <value>");
                    System.err.println("       help\n");
                }

                return null;
            }

            @Override
            public void onCommand(String command) {
                SimpleEntry cmd = this.parseCommand(command);
                if (cmd != null) {
                    result.put(cmd.getKey(), cmd.getValue());
                    if (!cmd.getKey().toString().equals("_saved")) {
                        result.remove("_saved");
                    }
                }
            }

            @Override
            public Boolean isTerminated() {
                return this.terminated;
            }
        });
        
        console.run();
        // Process
        if (result.get("_saved") != null) {
            result.remove("_saved");
            System.err.println("Result: " + result);
        } else {
            System.err.println("Note: changes were not saved. Please issue \"save\" command before \"done\".");
        }
    }


    /**
     * Write an application info.
     * 
     * @throws Exception 
     */
    public void doLoadFromFile() throws Exception {
        System.err.println("Load from the file is not yet implemented. :-(");
    }
}
