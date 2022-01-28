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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static java.lang.String.format;
import static me.bvn13.sewy.ClientListenerFactory.createClientListenerConstructor;

/**
 * TCP Server.
 * Works with command protocol.
 * Create the instance of this class to connect to {@link Client}
 */
public class CommandServer extends Server<CommandClientListener> {

    /**
     * @param host host to bind in order to start listen to clients
     * @param port port to start listen to
     */
    public CommandServer(String host, int port) {
        this(host, port, CommandClientListener.class);
    }

    /**
     * @param host host to bind in order to start listen to clients
     * @param port port to start listen to
     * @param clientListenerClass client listen class to be used for communication
     */
    public CommandServer(String host, int port, Class clientListenerClass) {
        this(host, port, createClientListenerConstructor(clientListenerClass));
    }

    /**
     *
     * @param host host to bind in order to start listen to clients
     * @param port port to start listen to
     * @param clientListenerConstructor to provide constructor for client listener (see {@link CommandServer#CommandServer(String, int, Class)})
     */
    @SuppressWarnings("unchecked")
    public CommandServer(String host, int port, Function<Socket, CommandClientListener> clientListenerConstructor) {
        log.debug("Starting server");
        executor.execute(() -> {
            try (final ServerSocket server = new ServerSocket(port, 0, InetAddress.getByName(host))) {

                socket = server;

                while (!server.isClosed()) {
                    final Socket client = server.accept();
                    final CommandClientListener clientListener = clientListenerConstructor.apply(client);
                    executor.execute(clientListener);
                    clients.add(clientListener);
                }

            } catch (IOException e) {
                log.error(format("Error while conversation with %s:%d", host, port), e);
            }
        });
    }

    /**
     * Sends command to every client
     * @param command command to be sent
     * @param <T> generic type
     */
    public <T extends AbstractCommand> void send(T command) {
        log.debug("Start to send command: " + command);
        for (CommandClientListener client : clients) {
            client.send(command);
        }
    }

}
