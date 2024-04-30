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
public class HelloTest2 {

    static Wiser wiser;
    static Client client;


    @BeforeAll
    @SneakyThrows
    static void init() {
        wiser = TestWiser.init();

        wiser.start();
    }

    /**
     *
     */
    @Test
    void testHelloCommand() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
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
    public void testHelloReset() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
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
    public void testEhloSize() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
            wiser.getServer().setMaxMessageSize(1000);
            client.expect("220");

            client.send("EHLO foo.com");
            client.expectContains("250-SIZE 1000");
        });
    }
}
