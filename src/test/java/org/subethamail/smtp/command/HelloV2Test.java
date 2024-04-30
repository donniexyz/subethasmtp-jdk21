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
class HelloV2Test {

    static Wiser wiser;
    static Client client;
    static int port = TestWiser.PORT + 4;


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
    void testHelloCommand() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", port);
            client.expect("220");

            client.send("HELO");
            client.expect("501 Syntax: HELO <hostname>");

            client.send("HELO");
            client.expect("501 Syntax: HELO <hostname>");

            // Correct!
            client.send("HELO foo.com");
            client.expect("250");

            // Correct!
            client.send("HELO foo.com");
            client.expect("250");
        });
    }

    /**
     *
     */
    @Test
    void testHelloReset() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", port);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("MAIL FROM: test@foo.com");
            client.expect("250 Ok");

            client.send("RSET");
            client.expect("250 Ok");

            client.send("MAIL FROM: test@foo.com");
            client.expect("250 Ok");
        });
    }

    /**
     *
     */
    @Test
    void testEhloSize() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", port);
            wiser.getServer().setMaxMessageSize(1000);
            client.expect("220");

            client.send("EHLO foo.com");
            client.expectContains("250-SIZE 1000");
        });
    }
}
