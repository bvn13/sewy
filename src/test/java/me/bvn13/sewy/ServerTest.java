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
import me.bvn13.sewy.command.ComplexCommand;
import me.bvn13.sewy.command.PingCommand;
import me.bvn13.sewy.command.PongCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class ServerTest {

    private static final int START_PORT = 12345;

    @ParameterizedTest
    @ValueSource(ints = START_PORT + 1)
    void testServerStarts(int port) throws InterruptedException {
        Server server = new Server("localhost", port, SimpleClientListener.class);
        Thread.sleep(1000);
        Assertions.assertTrue(server.isListening());
        server.stop();
    }

    @ParameterizedTest
    @ValueSource(ints = START_PORT + 2)
    void givenServerRunning_whenClientConnects_thenServerCanStopClientListener(int port) throws InterruptedException {
        Server server = new Server("localhost", port, SimpleClientListener.class);
        Client<SimpleClientListener> client = new Client<>("localhost", port, SimpleClientListener.class);
        Thread.sleep(1000);
        Assertions.assertTrue(server.isListening());
        Assertions.assertTrue(client.isConnected());
        server.stop();
        client.stop();
    }

    @ParameterizedTest
    @ValueSource(ints = START_PORT + 3)
    void failedToStartServerWithBadClientListener(int port) {
        Assertions.assertThrows(RuntimeException.class, () -> {
            new Server("localhost", port, Object.class);
        }, "Wrong client listener");
    }

    @ParameterizedTest
    @ValueSource(ints = START_PORT + 4)
    void serverIsAbleToStartWithLambdaProvidedClientListener(int port) throws InterruptedException {
        Server server = new Server("localhost", port, SimpleClientListener.class);
        Thread.sleep(1000);
        Assertions.assertTrue(server.isListening());
        server.stop();
    }

    @ParameterizedTest
    @ValueSource(ints = START_PORT + 5)
    void simpleEchoClientServer(int port) {
        new Server("192.168.0.153", port, EchoClientListener.class);
        Client<SimpleClientListener> client = new Client<>("192.168.0.153", port, SimpleClientListener.class);
        client.writeLine("hello");
        String response1 = client.readLine();
        Assertions.assertEquals("hello", response1);
        client.writeLine("olleh");
        String response2 = client.readLine();
        Assertions.assertEquals("olleh", response2);
    }

    @ParameterizedTest
    @ValueSource(ints = START_PORT + 6)
    void serverIsAbleToPingPong(int port) throws Exception {
        Sewy.register(PingCommand.class);
        Sewy.register(PongCommand.class);

        CommandServer server = new CommandServer("localhost", port, (socket) -> new CommandClientListener(socket) {
            @Override
            public AbstractCommand onCommand(AbstractCommand command) {
                if (command instanceof PingCommand) {
                    return new PongCommand((PingCommand) command);
                }
                throw new IllegalArgumentException(command.toString());
            }
        });

        AtomicLong latency = new AtomicLong(0);
        CommandClient client = new CommandClient("localhost", port, (socket) -> new CommandClientListener(socket) {
            @Override
            public AbstractCommand onCommand(AbstractCommand command) {
                if (command instanceof PongCommand) {
                    latency.set(((PongCommand)command).getLatency());
                    return null;
                } else {
                    throw new IllegalArgumentException(command.toString());
                }
            }
        });
        client.send(new PingCommand());
        Thread.sleep(1000);
        Assertions.assertTrue(latency.get() > 0);
    }

    @ParameterizedTest
    @ValueSource(ints = START_PORT + 7)
    void wideSeparatorTest(int port) throws Exception {
        Sewy.register(ComplexCommand.class);
        Sewy.setSeparator(new byte[] { '\n', 'e', 'n', 'd', '\n' });

        AtomicReference<ComplexCommand> check = new AtomicReference<>();

        CommandServer server = new CommandServer("localhost", port, (socket) -> new CommandClientListener(socket) {
            @Override
            public AbstractCommand onCommand(AbstractCommand command) {
                if (command instanceof ComplexCommand) {
                    check.set((ComplexCommand) command);
                    return null;
                }
                throw new IllegalArgumentException(command.toString());
            }
        });

        CommandClient client = new CommandClient("localhost", port, (socket) -> new CommandClientListener(socket) {
            @Override
            public AbstractCommand onCommand(AbstractCommand command) {
                throw new IllegalArgumentException(command.toString());
            }
        });

        ComplexCommand command = new ComplexCommand();
        command.add(new ComplexCommand.SimpleData("a1"));
        command.add(new ComplexCommand.SimpleData("b2"));
        command.add(new ComplexCommand.SimpleData("finish"));

        client.send(command);
        Thread.sleep(1000);
        Assertions.assertNotNull(check.get());
        Assertions.assertEquals(3, check.get().getDatum().size());
        Assertions.assertEquals("a1", check.get().getDatum().get(0).getString());
        Assertions.assertEquals("b2", check.get().getDatum().get(1).getString());
        Assertions.assertEquals("finish", check.get().getDatum().get(2).getString());
    }
}
