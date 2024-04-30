package org.subethamail.smtp.util;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A crude telnet client that can be used to send SMTP messages and test
 * the responses.
 *
 * @author Jeff Schnitzer
 * @author Jon Stevens
 */
public class Client implements Closeable {
    Socket socket;
    BufferedReader reader;
    PrintWriter writer;

    /**
     * Establishes a connection to host and port.
     *
     * @throws IOException
     * @throws UnknownHostException
     */
    public Client(String host, int port) throws UnknownHostException, IOException {
        this.socket = new Socket(host, port);
        this.writer = new PrintWriter(this.socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    /**
     * Sends a message to the server, ie "HELO foo.example.com". A newline will
     * be appended to the message.
     */
    public void send(String msg) {
        // Force \r\n since println() behaves differently on different platforms
        this.writer.print(msg + "\r\n");
        this.writer.flush();
    }

    /**
     * Throws an exception if the response does not start with
     * the specified string.
     */
    public void expect(String expect) throws Exception {
        String response = this.readResponse();
        if (!response.startsWith(expect))
            throw new Exception("Got: " + response + " Expected: " + expect);
    }

    /**
     * Throws an exception if the response does not contain
     * the specified string.
     */
    public void expectContains(String expect) throws Exception {
        String response = this.readResponse();
        if (!response.contains(expect))
            throw new Exception("Got: " + response + " Expected to contain: " + expect);
    }

    /**
     * Get the complete response, including a multiline response.
     * Newlines are included.
     */
    protected String readResponse() throws Exception {
        StringBuilder builder = new StringBuilder();
        boolean done = false;
        while (!done) {
            String line = this.reader.readLine();
            if (line.charAt(3) != '-')
                done = true;

            builder.append(line);
            builder.append('\n');
        }

        return builder.toString();
    }

    /**
     *
     */
    public void close() throws IOException {
        if (!this.socket.isClosed())
            this.socket.close();
    }
}
