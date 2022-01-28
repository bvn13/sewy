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

import me.bvn13.sewy.command.AbstractCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.function.Function;

import static java.lang.String.format;
import static me.bvn13.sewy.ClientListenerFactory.createClientListenerConstructor;

/**
 * TCP Client.
 * Works with command protocol.
 * Create the instance of this class to connect to {@link Server}
 */
public class CommandClient extends Client<CommandClientListener> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Default constructor is to delay connecting to Server
     */
    public CommandClient() {
    }

    /**
     * Starts to connect to server immediately
     * @param host host to connect to
     * @param port port to be used while connecting
     */
    public CommandClient(String host, int port) {
        this(host, port, CommandClientListener.class);
    }

    /**
     * Starts to connect to server immediately
     * @param host host to connect to
     * @param port port to be used while connecting
     * @param clientListenerClass client listener class describing protocol of communications
     */
    public CommandClient(String host, int port, Class clientListenerClass) {
        this(host, port, createClientListenerConstructor(clientListenerClass));
    }

    /**
     * Connects to server immediately
     * @param host host to connect to
     * @param port port to be used while connecting
     * @param clientListenerConstructor to provide constructor for client listener (see {@link me.bvn13.sewy.Client#Client(java.lang.String, int, java.lang.Class)})
     */
    public CommandClient(String host, int port, Function<Socket, CommandClientListener> clientListenerConstructor) {
        log.debug("Creating client");
        connect(host, port, clientListenerConstructor);
    }

    /**
     * Sends command to server
     * @param command command to be sent
     * @param <T> generic type
     */
    public <T extends AbstractCommand> void send(T command) {
        log.debug("Start to send command: " + command);
        client.send(command);
    }
}
