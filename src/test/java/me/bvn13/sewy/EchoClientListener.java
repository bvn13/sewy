/*
   Copyright 2022 Vyacheslav Boyko

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package me.bvn13.sewy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

/**
 * Simple ECHOed client listener
 * Writes into socket all the data received before
 */
public class EchoClientListener extends AbstractClientListener {

    private final Logger log = LoggerFactory.getLogger(EchoClientListener.class);

    public EchoClientListener(Socket socket) {
        super(socket);
    }

    /**
     * Thread runner
     */
    @Override
    public void run() {
        while (socket.isConnected()) {
            Thread.yield();
            try {
                final String data = readLine();
                writeLine(data);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }
}
