package org.subethamail.smtp.command;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.subethamail.smtp.helper.TestWiser;
import org.subethamail.smtp.util.Client;
import org.subethamail.wiser.Wiser;

/**
 * @author Dony Zulkarnaen
 */
class ReceiptV2Test {
    static Wiser wiser;
    static Client client;
    static int port = TestWiser.PORT + 7;


    @BeforeAll
    @SneakyThrows
    static void init() {
        wiser = TestWiser.init(port);

        wiser.start();
    }

    /**
     *
     */
    @Test
    void testReceiptBeforeMail() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", port);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("RCPT TO: bar@foo.com");
            client.expect("503 5.5.1 Error: need MAIL command");
        });
    }

    /**
     *
     */
    @Test
    void testReceiptErrorInParams() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", port);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("MAIL FROM: success@subethamail.org");
            client.expect("250 Ok");

            client.send("RCPT");
            client.expect("501 Syntax: RCPT TO: <address>  Error in parameters:");
        });
    }

    /**
     *
     */
    @Test
    void testReceiptAccept() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", port);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("MAIL FROM: success@subethamail.org");
            client.expect("250 Ok");

            client.send("RCPT TO: failure@subethamail.org");
            client.expect("553 <failure@subethamail.org> address unknown.");

            client.send("RCPT TO: success@subethamail.org");
            client.expect("250 Ok");
        });
    }

    /**
     *
     */
    @Test
    void testReceiptNoWhiteSpace() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", port);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("MAIL FROM: success@subethamail.org");
            client.expect("250 Ok");

            client.send("RCPT TO:success@subethamail.org");
            client.expect("250 Ok");
        });
    }
}
