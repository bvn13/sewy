package me.bvn13.sewy;

import java.net.Socket;

public class EchoClientListener extends AbstractClientListener {
    public EchoClientListener(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        writeLine(readLine());
    }
}
