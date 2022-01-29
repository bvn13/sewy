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
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.net.Socket;

import static java.lang.String.format;
import static me.bvn13.sewy.Sewy.getSeparator;

/**
 * Client listener describing protocol-oriented communication
 */
public class CommandClientListener extends AbstractClientListener implements AbstractCommandExecutor {
    public CommandClientListener(Socket socket) {
        super(socket);
    }

    /**
     * Thread runner
     */
    @Override
    public void run() {
        for (Thread.yield(); !socket.isConnected() && !socket.isClosed(); Thread.yield()) {
        }
        while (socket.isConnected()) {
            Thread.yield();
            byte[] line = readBytes(getSeparator());
            if (line == null || line.length == 0) {
                continue;
            }
            final Object command;
            try {
                command = SerializationUtils.deserialize(line);
            } catch (Throwable e) {
                log.warn("Deserialization exception occurred!", e);
                continue;
            }
            if (command == null) {
                continue;
            }
            if (!Sewy.getRegisteredDataTypes().contains(command.getClass())) {
                log.error("Unexpected command received");
                continue;
            }
            log.debug("Command received: " + command);
            if (!(command instanceof AbstractCommand)) {
                log.warn("Incorrect command received: " + command);
                continue;
            }
            final Serializable response = onCommand((AbstractCommand) command);
            log.debug(format("Response for %s is: %s", command, response));
            writeBytes(SerializationUtils.serialize(response), getSeparator());
        }
    }


    /**
     * Method to receive the data command-by-command incoming from clients
     * You need to override it
     *
     * @param command serialized command to be checked by using
     *                <p>{@code instanceof ConcreteCommandClass}</p>
     * @return server answer on client command
     */
    public AbstractCommand onCommand(AbstractCommand command) {
        return null;
    }

    /**
     * Sends command to opposite side
     * @param command command to be sent
     * @param <T> generic type
     */
    public <T extends AbstractCommand> void send(T command) {
        log.debug("Start to send command: " + command);
        writeBytes(SerializationUtils.serialize(command), getSeparator());
    }

}
