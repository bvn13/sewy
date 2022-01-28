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

import java.io.IOException;
import java.lang.reflect.Constructor;
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

public class Server {

    private static final Logger log = LoggerFactory.getLogger(Server.class);

    private final ExecutorService executor = Executors.newCachedThreadPool();
    final List<AbstractClientListener> clients = Collections.synchronizedList(new ArrayList<>());

    private ServerSocket socket;

    public Server(String host, int port) {
        this(host, port, DefaultClientListener.class);
    }

    @SuppressWarnings("unchecked")
    public Server(String host, int port, Class clientListenerClass) {
        this(host, port, defaultClientListenerConstructor(clientListenerClass));
    }

    @SuppressWarnings("unchecked")
    public Server(String host, int port, Function<Socket, AbstractClientListener> clientListenerConstructor) {

        executor.execute(() -> {
            try (final ServerSocket server = new ServerSocket(port, 0, InetAddress.getByName(host))) {

                socket = server;

                while (!server.isClosed()) {
                    final Socket client = server.accept();
                    final AbstractClientListener clientListener = clientListenerConstructor.apply(client);
                    executor.execute(clientListener);
                    clients.add(clientListener);
                }

            } catch (IOException e) {
                log.error(format("Error while conversation with %s:%d", host, port), e);
            }
        });

    }

    public void stop() {
        final Iterator<AbstractClientListener> iterator = clients.iterator();
        while (iterator.hasNext()) {
            final AbstractClientListener client = iterator.next();
            client.stop();
            iterator.remove();
        }
        executor.shutdown();
    }

    boolean isListening() {
        return socket != null && socket.isBound();
    }

    @SuppressWarnings("unchecked")
    private static Function<Socket, AbstractClientListener> defaultClientListenerConstructor(Class clientListenerClass) {

        if (clientListenerClass.getGenericSuperclass() == null
                || !clientListenerClass.getGenericSuperclass().equals(AbstractClientListener.class)) {
            throw new IllegalArgumentException("Wrong client listener of type: "+clientListenerClass.getName());
        }

        return (client) -> {
            try {
                final Constructor<AbstractClientListener> constructor = clientListenerClass.getDeclaredConstructor(Socket.class);
                constructor.setAccessible(true);
                return constructor.newInstance(client);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

    }

}
