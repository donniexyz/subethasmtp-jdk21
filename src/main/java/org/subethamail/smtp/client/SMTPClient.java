package org.subethamail.smtp.client;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.io.DotTerminatedOutputStream;
import org.subethamail.smtp.io.ExtraDotOutputStream;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;


/**
 * A very low level abstraction of the STMP stream which knows how to handle
 * the raw protocol for lines, whitespace, etc.
 *
 * @author Jeff Schnitzer
 */
@SuppressWarnings({"DuplicateThrows", "unused"})
public class SMTPClient {

    private static final String ERR_MESSAGE_MALFORMED_REPLY = "Malformed SMTP reply: ";
    /** 5 minutes */
    private static final int CONNECT_TIMEOUT = 300 * 1000;

    /** 10 minutes */
    private static final int REPLY_TIMEOUT = 600 * 1000;

    /** */
    private static final Logger log = LoggerFactory.getLogger(SMTPClient.class);

    /** the local socket address
     * -- GETTER --
     * @return Returns the local socket address.
     */
    @Getter
    private SocketAddress bindpoint;

    /**
     * True if the client has been successfully connected to the server and not
     * it has not been closed yet.
     * <p>
     * -- GETTER --
     * @return Returns true if the client is connected to the server.
     */
    @Getter
    private boolean connected;

    /** Just for display purposes
     * -- GETTER --
     *
     * @return a nice pretty description of who we are connected to
     */
    @Getter
    String hostPort;

    /** The raw socket */
    Socket socket;

    /** */
    BufferedReader reader;

    /** Output streams used for data */
    OutputStream rawOutput;
    /**
     * A stream which wraps {@link #rawOutput} and is used to write out the DOT
     * CR LF terminating sequence in the DATA command, if necessary
     * complementing the message content with a closing CR LF.
     */
    DotTerminatedOutputStream dotTerminatedOutput;
    /**
     * This stream wraps {@link #dotTerminatedOutput} and it does the dot
     * stuffing for the SMTP DATA command.
     */
    ExtraDotOutputStream dataOutput;

    /** Note we bypass this during DATA */
    PrintWriter writer;

    /**
     * Result of an SMTP exchange.
     */
    @Getter
    public static class Response {
        int code;
        String message;

        public Response(int code, String text) {
            this.code = code;
            this.message = text;
        }

        public boolean isSuccess() {
            return this.code >= 100 && this.code < 400;
        }

        @Override
        public String toString() {
            return this.code + " " + this.message;
        }
    }

    /**
     * Creates an unconnected client.
     */
    public SMTPClient() {
        // nothing to do
    }

    /**
     * Establishes a connection to host and port.
     *
     * @throws UnknownHostException
     *             if the hostname cannot be resolved
     * @throws IOException
     *             if there is a problem connecting to the port
     */
    public SMTPClient(String host, int port) throws UnknownHostException, IOException {
        this(host, port, null);
    }

    /**
     * Establishes a connection to host and port from the specified local socket
     * address.
     *
     * @param bindpoint
     *            the local socket address. If null, the system will pick up an
     *            ephemeral port and a valid local address.
     * @throws UnknownHostException
     *             if the hostname cannot be resolved
     * @throws IOException
     *             if there is a problem connecting to the port
     */
    public SMTPClient(String host, int port, SocketAddress bindpoint) throws UnknownHostException, IOException {
        this.bindpoint = bindpoint;
        connect(host, port);
    }

    /**
     * Establishes a connection to host and port.
     *
     * @throws IOException
     *             if there is a problem connecting to the port
     */
    public void connect(String host, int port) throws IOException {
        if (connected)
            throw new IllegalStateException("Already connected");

        if (this.hostPort == null)
            this.hostPort = host + ":" + port;

        if (log.isDebugEnabled())
            log.debug("Connecting to {}", this.hostPort);

        this.socket = createSocket();
        this.socket.bind(this.bindpoint);
        this.socket.setSoTimeout(REPLY_TIMEOUT);
        this.socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT);

        try {
            this.bindpoint = this.socket.getLocalSocketAddress();

            this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            this.rawOutput = this.socket.getOutputStream();
            this.dotTerminatedOutput = new DotTerminatedOutputStream(this.rawOutput);
            this.dataOutput = new ExtraDotOutputStream(this.dotTerminatedOutput);
            this.writer = new PrintWriter(this.rawOutput, true);
        } catch (IOException e) {
            close();
            throw e;
        }

        connected = true;
    }

    /**
     * Returns a new unconnected socket.
     * <p>
     * Implementation notice for subclasses: This function is called by the
     * constructors which open the connection immediately. In these cases the
     * subclass is not yet initialized, therefore subclasses overriding this
     * function shouldn't use those constructors.
     */
    protected Socket createSocket() {
        return new Socket();
    }

    /**
     * Sends a message to the server, ie "HELO foo.example.com". A newline will
     * be appended to the message.
     *
     * @param msg should not have any newlines
     */
    protected void send(String msg) throws IOException {
        if (log.isDebugEnabled())
            log.debug("Client: {}", msg);
        if (!connected)
            throw new IllegalStateException("Not connected");

        // Force \r\n since println() behaves differently on different platforms
        this.writer.print(msg + "\r\n");
        this.writer.flush();
    }

    /**
     * Note that the response text comes back without trailing newlines.
     */
    protected Response receive() throws IOException {
        if (!connected)
            throw new IllegalStateException("Not connected");

        StringBuilder builder = new StringBuilder();
        String line;

        boolean done = false;
        do {
            line = this.reader.readLine();
            if (line == null) {
                if (builder.isEmpty())
                    throw new EOFException("Server disconnected unexpectedly, no reply received");
                else
                    throw new IOException(ERR_MESSAGE_MALFORMED_REPLY + builder);
            }

            if (log.isDebugEnabled())
                log.debug("Server: {}", line);

            if (line.length() < 4)
                throw new IOException(ERR_MESSAGE_MALFORMED_REPLY + line);
            builder.append(line.substring(4));

            if (line.charAt(3) == '-')
                builder.append('\n');
            else
                done = true;
        }
        while (!done);

        int code;
        String codeString = line.substring(0, 3);
        try {
            code = Integer.parseInt(codeString);
        } catch (NumberFormatException e) {
            throw new IOException(ERR_MESSAGE_MALFORMED_REPLY + line, e);
        }

        return new Response(code, builder.toString());
    }

    /**
     * Sends a message to the server, ie "HELO foo.example.com". A newline will
     * be appended to the message.
     *
     * @param msg should not have any newlines
     * @return the response from the server
     */
    public Response sendReceive(String msg) throws IOException {
        this.send(msg);
        return this.receive();
    }

    /** If response is not success, throw an exception */
    public Response receiveAndCheck() throws IOException, SMTPException {
        Response resp = this.receive();
        if (!resp.isSuccess())
            throw new SMTPException(resp);
        return resp;
    }

    /** If response is not success, throw an exception */
    public Response sendAndCheck(String msg) throws IOException, SMTPException {
        this.send(msg);
        return this.receiveAndCheck();
    }

    /** Logs but otherwise ignores errors */
    public void close() {
        connected = false;

        if (this.socket != null && !this.socket.isClosed()) {
            try {
                this.socket.close();

                if (log.isDebugEnabled())
                    log.debug("Closed connection to {}", this.hostPort);
            } catch (IOException ex) {
                log.error("Problem closing connection to {}", this.hostPort, ex);
            }
        }
    }

    /** */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " { " + this.hostPort + "}";
    }

    /**
     * Sets the local socket address. If null, the system will pick up an
     * ephemeral port and a valid local address. Default is null.
     */
    public void setBindpoint(SocketAddress bindpoint) {
        this.bindpoint = bindpoint;
    }

    /**
     * Sets the name of the remote MTA for informative purposes. Default is
     * host:port, where host and port are the values which were used to open the
     * TCP connection to the server, as they were passed to the connect method.
     */
    public void setHostPort(String hostPort) {
        this.hostPort = hostPort;
    }

}
