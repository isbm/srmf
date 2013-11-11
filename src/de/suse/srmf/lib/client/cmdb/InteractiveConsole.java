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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class takes a setup of a map with parameter and its description,
 * returning back a map with these parameters and the values set instead of description.
 * 
 * @author bo
 */
public class InteractiveConsole {
    public static interface ConsoleCallback {
        public void onCommand(String command);
        public Boolean isTerminated();
    }

    private Boolean isTerminated;
    private final String prompt;
    private List<InteractiveConsole.ConsoleCallback> callbacks;

    public InteractiveConsole(String prompt) {
        this.prompt = prompt != null ? prompt : "> ";
        this.callbacks = new ArrayList<ConsoleCallback>();
    }
    
    public void addConsoleCallback(ConsoleCallback callback) {
        if (callback != null) {
            this.callbacks.add(callback);
        }
    }

    /**
     * Run interactive input.
     * 
     * @return 
     */
    public void run() {
        Map response = new HashMap();
        this.isTerminated = false;
        while (!this.isTerminated) {
            System.out.print(this.prompt);
            String command = System.console().readLine();
            if (command == null || command.isEmpty()) {
                continue;
            }

            for (int i = 0; i < callbacks.size(); i++) {
                InteractiveConsole.ConsoleCallback consoleCallback = callbacks.get(i);
                consoleCallback.onCommand(command);
                this.isTerminated = consoleCallback.isTerminated();
            }
        }
    }
}
