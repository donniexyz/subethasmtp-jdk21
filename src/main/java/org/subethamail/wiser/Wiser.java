/*
 * $Id$
 * $URL$
 */

package org.subethamail.wiser;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Wiser is a tool for unit testing applications that send mail.  Your unit
 * tests can start Wiser, run tests which generate emails, then examine the
 * emails that Wiser received and verify their integrity.
 * <p>
 * Wiser is not intended to be a "real" mail server and is not adequate
 * for that purpose; it simply stores all mail in memory.  Use the
 * MessageHandlerFactory interface (optionally with the SimpleMessageListenerAdapter)
 * of SubEthaSMTP instead.
 *
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
@Getter
public class Wiser implements SimpleMessageListener {
    /** */
    private static final Logger log = LoggerFactory.getLogger(Wiser.class);

    /**
     * The server implementation
     */
    SMTPServer server;

    /**
     * -- GETTER --
     *  Returns the list of WiserMessages.
     *  <p>
     *  The number of mail transactions and the number of mails may be different.
     *  If a message is received with multiple recipients in a single mail
     *  transaction, then the list will contain more WiserMessage instances, one
     *  for each recipient.
     */
    protected List<WiserMessage> messages = Collections.synchronizedList(new ArrayList<>());

    /**
     * Create a new SMTP server with this class as the listener.
     * The default port is 25. Call setPort()/setHostname() before
     * calling start().
     */
    public Wiser() {
        this.server = new SMTPServer(new SimpleMessageListenerAdapter(this));
    }

    /** Convenience constructor */
    public Wiser(int port) {
        this();
        this.setPort(port);
    }

    /**
     * The port that the server should listen on.
     * @param port
     */
    public void setPort(int port) {
        this.server.setPort(port);
    }

    /**
     * The hostname that the server should listen on.
     * @param hostname
     */
    public void setHostname(String hostname) {
        this.server.setHostName(hostname);
    }

    /** Starts the SMTP Server */
    public void start() {
        this.server.start();
    }

    /** Stops the SMTP Server */
    public void stop() {
        this.server.stop();
    }

    /** A main() for this class. Starts up the server. */
    public static void main(String[] args) throws Exception {
        Wiser wiser = new Wiser();
        wiser.start();
    }

    /** Always accept everything */
    public boolean accept(String from, String recipient) {
        if (log.isDebugEnabled())
            log.debug("Accepting mail from {} to {}", from, recipient);

        return true;
    }

    /** Cache the messages in memory */
    public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException {
        if (log.isDebugEnabled())
            log.debug("Delivering mail from {} to {}", from, recipient);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        data = new BufferedInputStream(data);

        // read the data from the stream
        int current;
        while ((current = data.read()) >= 0) {
            out.write(current);
        }

        byte[] bytes = out.toByteArray();

        if (log.isDebugEnabled())
            log.debug("Creating message from data with {} bytes", bytes.length);

        // create a new WiserMessage.
        this.messages.add(new WiserMessage(this, from, recipient, bytes));
    }

    /**
     * Creates the JavaMail Session object for use in WiserMessage
     */
    protected Session getSession() {
        return Session.getDefaultInstance(new Properties());
    }

    /**
     * For debugging purposes, dumps a rough outline of the messages to the output stream.
     */
    public void dumpMessages(PrintStream out) throws MessagingException {
        out.println("----- Start printing messages -----");

        for (WiserMessage wmsg : this.getMessages())
            wmsg.dumpMessage(out);

        out.println("----- End printing messages -----");
    }
}
