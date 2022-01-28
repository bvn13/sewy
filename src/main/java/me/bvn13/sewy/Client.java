/*
   Copyright 2020 Vyacheslav Boyko

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;

public class Client {

    private static final Logger log = LoggerFactory.getLogger(Client.class);

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public Client() {
    }

    public Client(String host, int port) {
        connect(host, port);
    }

    public void stop() {
        out.close();
        try {
            in.close();
        } catch (IOException e) {
            log.warn("Unable to close IN client buffer");
        }
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void connect(String host, int port) {
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            log.error(format("Error while conversation with %s:%d", host, port), e);
            stop();
            return;
        }
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readLine() {
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeLine(String data) {
        out.println(data);
    }

    boolean isConnected() {
        return socket.isConnected();
    }

}
