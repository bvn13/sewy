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

import java.lang.reflect.Constructor;
import java.net.Socket;
import java.util.function.Function;

/**
 * Factory constructing client listeners
 */
class ClientListenerFactory {

    /**
     * Creates client listener constructor
     *
     * @param clientListenerClass class to be used as client listener
     * @param <T>                 generic type
     * @return lambda method to create client listener
     */
    @SuppressWarnings("unchecked")
    static <T extends AbstractClientListener> Function<Socket, T> createClientListenerConstructor(Class clientListenerClass) {

        if (clientListenerClass.getGenericSuperclass() == null) {
            throw new IllegalArgumentException("Wrong client listener of type: " + clientListenerClass.getName());
        }

        return (client) -> {
            try {
                final Constructor<CommandClientListener> constructor = clientListenerClass.getDeclaredConstructor(Socket.class);
                constructor.setAccessible(true);
                return (T) constructor.newInstance(client);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}
