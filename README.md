# Sew'y [səʊi]

## What is it?

The lightweight network library providing client-server communications that could be described with commands.

## Why another network library?

1. other protocol-based network libraries are old (in e. [kryonet](https://github.com/EsotericSoftware/kryonet))
2. or they are heavy and very overloaded

## How to use?

### Simple ECHO server

1. Describe ECHOed client listener

https://github.com/bvn13/sewy/blob/380dd5f8b0757791f583852c1e1516c9470baf42/src/test/java/me/bvn13/sewy/EchoClientListener.java#L24-L40

2. Create Server

```java
new Server("192.168.0.153", port, EchoClientListener.class);

```

3. Create Client to communicate with server

```java
Client<SimpleClientListener> client = new Client<>("192.168.0.153", port, SimpleClientListener.class);
```

4. Send raw data and read the response

```java
client.writeLine("hello");
String response1 = client.readLine();
Assertions.assertEquals("hello", response1);
```

### Command-based client listener

1. Implement commands inheriting from `AbstractCommand`
2. Register all commands as white listed

```java
Sewy.register(PingCommand.class);
Sewy.register(PongCommand.class);
```

3. Start server with `CommandClientListener` implementing response creation logic

```java
Server server = new Server("localhost", port, (socket) -> new CommandClientListener(socket) {
    @Override
    public AbstractCommand onCommand(AbstractCommand command) {
        if (command instanceof PingCommand) {
            return new PongCommand((PingCommand) command);
        }
        throw new IllegalArgumentException(command.toString());
    }
});
```

4. Start client to send commands to server

```java
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

```