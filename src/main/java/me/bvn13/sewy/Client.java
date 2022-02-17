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

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static java.lang.String.format;
import static me.bvn13.sewy.ClientListenerFactory.createClientListenerConstructor;

/**
 * TCP Client.
 * Create the instance of this class to connect to {@link Server}
 */
public class Client<T extends AbstractClientListener> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ExecutorService executor = Executors.newCachedThreadPool();
    protected T client;

    protected Socket socket;

    /**
     * Default constructor is to delay connecting to server
     */
    public Client() {
    }

    /**
     * Connects to server immediately
     * @param host host to connect to
     * @param port port to be used while connecting
     * @param clientListenerClass client listener class describing protocol of communications
     */
    public Client(String host, int port, Class clientListenerClass) {
        this(host, port, createClientListenerConstructor(clientListenerClass));
    }

    /**
     * Connects to server immediately
     * @param host host to connect to
     * @param port port to be used while connecting
     * @param clientListenerConstructor to provide constructor for client listener (see {@link me.bvn13.sewy.Client#Client(java.lang.String, int, java.lang.Class)})
     */
    public Client(String host, int port, Function<Socket, T> clientListenerConstructor) {
        log.debug("Creating client");
        connect(host, port, clientListenerConstructor);
    }

    /**
     * Connects to {@link Server}
     * @param host host to connect to
     * @param port port to be used while connecting
     * @param clientListenerConstructor to provide constructor for client listener (see {@link me.bvn13.sewy.Client#Client(java.lang.String, int, java.lang.Class)})
     */
    public void connect(String host, int port, Function<Socket, T> clientListenerConstructor) {
        try {
            log.debug(format("Connecting to %s:%d", host, port));
            socket = new Socket(host, port);
            client = clientListenerConstructor.apply(socket);
            executor.execute(client);
        } catch (IOException e) {
            log.error(format("Error while conversation with %s:%d", host, port), e);
            stop();
        }
    }

    /**
     * Stops client gracefully
     */
    public void stop() {
        log.debug("Stopping client");
        client.stop();
        try {
            socket.close();
        } catch (IOException e) {
            log.error("Failed to close socket");
        }
        executor.shutdown();
    }

    /**
     * To check whether socket is online
     * @return
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    /**
     * Reads one line from socket
     * @return the line read from socket
     */
    public String readLine() {
        return client.readLine();
    }

    /**
     * Reads data from socket until {@code separator} is encountered
     * @param separator
     * @return
     */
    public byte[] readBytes(byte separator) {
        return client.readBytes(separator);
    }

    /**
     * Writes line into socket ending with default separator '\n'.
     * @param data data to be sent into socket
     */
    public void writeLine(String data) {
        client.writeLine(data);
    }
}
