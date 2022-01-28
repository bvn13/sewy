package me.bvn13.sewy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ServerTest {

    private static final int START_PORT = 12345;

    @ParameterizedTest
    @ValueSource(ints = START_PORT + 1)
    void testServerStarts(int port) throws InterruptedException {
        Server server = new Server("localhost", port);
        Thread.sleep(1000);
        Assertions.assertTrue(server.isListening());
        server.stop();
    }

    @ParameterizedTest
    @ValueSource(ints = START_PORT + 2)
    void givenServerRunning_whenClientConnects_thenServerCanStopClientListener(int port) throws InterruptedException {
        Server server = new Server("localhost", port);
        Client client = new Client("localhost", port);
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
    void serverStartedWithLambdaProvidedClientListener(int port) throws InterruptedException {
        Server server = new Server("localhost", port, (socket) -> new AbstractClientListener(socket) {
            @Override
            public void run() {

            }
        });
        Thread.sleep(1000);
        Assertions.assertTrue(server.isListening());
        server.stop();
    }

    @ParameterizedTest
    @ValueSource(ints = START_PORT + 5)
    void simpleEchoClientServer(int port) {
        new Server("localhost", port, EchoClientListener.class);
        Client client = new Client("localhost", port);
        client.writeLine("hello");
        String response = client.readLine();
        Assertions.assertEquals("hello", response);
    }

}
