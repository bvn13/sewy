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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static me.bvn13.sewy.Sewy.getSeparator;

/**
 * TCP Client listener.
 * This class provides methods to manage communications between {@link Server} and {@link Client}.
 * Inherit this class to implement your own communication type.
 */
public abstract class AbstractClientListener implements Runnable {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected final Socket socket;
    protected OutputStream out;
    protected InputStream in;

    protected AbstractClientListener(Socket socket) {
        log.debug("Initializing client listener");
        this.socket = socket;
        try {
            this.in = socket.getInputStream();
            log.debug("BufferedReader successfully created");
            log.debug("PrintWriter successfully created");
            this.out = socket.getOutputStream();
            log.debug("OutputStream successfully created");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Thread runner.
     * Override it according the needs
     */
    @Override
    public abstract void run();

    /**
     * Reads line (separated with '\n') from socket
     *
     * @return the line read from socket
     */
    public String readLine() throws IOException {
        final byte[] bytes = readBytes(getSeparator());
        final StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append((char) aByte);
        }
        final String string = sb.toString();
        if (log.isTraceEnabled()) log.trace("Received: " + string);
        return string;
    }

    /**
     * Reads data from socket until {@code separator} is encountered
     *
     * @param separator byte to separate data portions
     * @return array of bytes read from socket
     */
    public byte[] readBytes(byte[] separator) throws IOException {
        final List<Byte> data = new ArrayList<>(2048 * 2048);
        List<Byte> buffer = new ArrayList<>(separator.length);
        int separatorPosition = 0;
        while (socket.isConnected() && !socket.isClosed()) {
            byte[] portion = in.readNBytes(1);
            if (portion == null || portion.length == 0) {
                break;
            }
            if (portion[0] == separator[separatorPosition]) {
                if (separatorPosition == separator.length - 1) {
                    break;
                }
                separatorPosition++;
                buffer.add(portion[0]);
                continue;
            } else {
                separatorPosition = 0;
                data.addAll(buffer);
                buffer.clear();
            }
            data.add(portion[0]);
        }
        final byte[] bytes = new byte[data.size()];
        int i = 0;
        for (Byte aByte : data) {
            bytes[i++] = aByte;
        }
        if (log.isTraceEnabled()) log.trace("Received {} bytes: {}", bytes.length, bytes);
        return bytes;
    }

    /**
     * Writes line into socket ending with default separator '\n'.
     * Flushes after writing.
     *
     * @param bytes     bytes to be sent into socket
     * @param separator byte to separate data portions
     */
    public void writeBytes(byte[] bytes, byte[] separator) throws IOException {
        if (log.isTraceEnabled()) log.trace("Sending {} bytes: {}", bytes.length, bytes);
        out.write(bytes);
        out.write(separator);
        out.flush();
    }

    /**
     * Writes line into socket ending with default separator '\n'.
     * Flushes after writing.
     *
     * @param data data to be sent into socket
     */
    public void writeLine(String data) {
        if (log.isTraceEnabled()) log.trace("Sending: " + data);
        try {
            writeBytes(data.getBytes(), getSeparator());
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Stops client listener gracefully
     */
    public void stop() {
        log.debug("Stopping");
        try {
            out.close();
            in.close();
        } catch (IOException e) {
            log.warn("Unable to close IN/OUT client buffer");
        }
        try {
            socket.close();
        } catch (IOException e) {
            log.warn("Unable to close socket");
        }
    }
}
