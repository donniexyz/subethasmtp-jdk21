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
class MailTest2 {
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
    void testMailNoHello() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
            client.expect("220");

            client.send("MAIL FROM: test@example.com");
            client.expect("250");
        });
    }

    /**
     *
     */
    @Test
    void testAlreadySpecified() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("MAIL FROM: test@example.com");
            client.expect("250 Ok");

            client.send("MAIL FROM: another@example.com");
            client.expect("503 5.5.1 Sender already specified.");
        });
    }

    /**
     *
     */
    @Test
    void testInvalidSenders() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            // added <> because without them "lkjk" is a parameter
            // to the MAIL command. (Postfix responds accordingly)
            client.send("MAIL FROM: <test@lkjsd lkjk>");
            client.expect("553 <test@lkjsd lkjk> Invalid email address.");
        });
    }

    /**
     *
     */
    @Test
    void testMalformedMailCommand() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("MAIL");
            client.expect("501 Syntax: MAIL FROM: <address>  Error in parameters:");
        });
    }

    /**
     *
     */
    @Test
    void testEmptyFromCommand() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("MAIL FROM: <>");
            client.expect("250");
        });
    }

    /**
     *
     */
    @Test
    void testEmptyEmailFromCommand() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("MAIL FROM:");
            client.expect("501 Syntax: MAIL FROM: <address>");
        });
    }

    /**
     *
     */
    @Test
    void testMailWithoutWhitespace() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("MAIL FROM:<validuser@subethamail.org>");
            client.expect("250 Ok");
        });
    }

    /**
     *
     */
    @Test
    void testSize() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
            wiser.getServer().setMaxMessageSize(1000);
            client.expect("220");

            client.send("EHLO foo.com");
            client.expectContains("250-SIZE 1000");

            client.send("MAIL FROM:<validuser@subethamail.org> SIZE=100");
            client.expect("250 Ok");
        });
    }

    /**
     *
     */
    @Test
    void testSizeWithoutSize() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
            wiser.getServer().setMaxMessageSize(1000);
            client.expect("220");

            client.send("EHLO foo.com");
            client.expectContains("250-SIZE 1000");

            client.send("MAIL FROM:<validuser@subethamail.org>");
            client.expect("250 Ok");
        });
    }

    /**
     *
     */
    @Test
    void testSizeTooLarge() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
            wiser.getServer().setMaxMessageSize(1000);
            client.expect("220");

            client.send("EHLO foo.com");
            client.expectContains("250-SIZE 1000");

            client.send("MAIL FROM:<validuser@subethamail.org> SIZE=1001");
            client.expect("552");
        });
    }

}

