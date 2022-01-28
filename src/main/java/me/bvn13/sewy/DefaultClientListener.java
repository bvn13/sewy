package me.bvn13.sewy;

import java.net.Socket;

public class DefaultClientListener extends AbstractClientListener {
    public DefaultClientListener(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {

    }
}
