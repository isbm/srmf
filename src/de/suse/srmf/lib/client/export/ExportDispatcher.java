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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Export dispatcher.
 * 
 * @author Bo Maryniuk <bo@suse.de>
 */
public class ExportDispatcher {
    public static final String TYPE_FILE = "file"; // Saves to the file, located in the destination host subdir.
    public static final String TYPE_CONSOLE = "console"; // Prints on the console using STDOUT or STDERR channels.
    public static final String CONSOLE_STDOUT = "stdout";
    public static final String CONSOLE_STDERR = "stderr";

    /**
     * Destination entity is to describe where and how to store the output.
     */
    public static final class DestinationEntity {
        private String mediaType;
        private String filename;

        /**
         * Constructor.
         * "Descriptor" has format "type:filename".
         * 
         * @param descriptor 
         */
        public DestinationEntity(String descriptor) throws Exception {
            if (descriptor == null) {
                descriptor = "";
            }
            
            if (descriptor.isEmpty()) {
                throw new Exception("Descriptor of the destination cannot be empty!");
            }
            
            String[] tokens = descriptor.split("\\:", 2);
            if (tokens.length != 2) {
                throw new Exception("Descriptor of the destination has wrong syntax. It is 'type:filename'.");
            }
            
            this.mediaType = tokens[0];
            this.filename = tokens[1];
        }

        /**
         * Filename of the object.
         * 
         * @return 
         */
        public String getFilename() {
            return filename;
        }

        /**
         * Media type of the object.
         * 
         * @return 
         */
        public String getMediaType() {
            return mediaType;
        }
    }

    private File root; // Export destination. Creates subdirectories per host.

    /**
     * Constructor.
     * 
     * @param destination 
     */
    public ExportDispatcher(File destination)
            throws IOException {
        if (!destination.exists()) {
            if (!destination.mkdirs()) {
                throw new IOException("Unable to write to %s".format(destination.getAbsolutePath()));
            }
        }

        this.root = destination;
    }


    /**
     * Dispatches the source to the destination.
     * Format of "out" parameter is: "destination-type:filename".
     * Parameter "reference" is 
     * @param source
     * @param reference 
     * @param out 
     */
    public void dispatch(String source, String reference, String out)
            throws IOException,
                   Exception {
        File destinationDirectory = new File(this.root.getAbsolutePath() + "/" + reference);
        if (!destinationDirectory.exists()) {
            if (!destinationDirectory.mkdirs()) {
                throw new IOException("Unable to write to %s".format(destinationDirectory.getAbsolutePath()));
            }
        }

        DestinationEntity destEntity = new DestinationEntity(out);
        if (destEntity.getMediaType().equals(ExportDispatcher.TYPE_FILE)) {
            File outfile = new File(destinationDirectory.getAbsolutePath() + "/" + destEntity.getFilename());
            if (outfile.exists()) {
                outfile.delete();
            }
            FileWriter writer = new FileWriter(outfile.getAbsoluteFile());
            BufferedWriter buff = new BufferedWriter(writer);
            buff.write(source);
            buff.close();
        } else if (destEntity.getMediaType().equals(ExportDispatcher.TYPE_CONSOLE)) {
            PrintStream stream = destEntity.getFilename().equals(ExportDispatcher.CONSOLE_STDOUT) ? System.out : System.err;
            for (int i = 0; i < 80; i++) {
                stream.print("=");
            }
            stream.print("\n" + source + "\n\n");
        }
    }
}
