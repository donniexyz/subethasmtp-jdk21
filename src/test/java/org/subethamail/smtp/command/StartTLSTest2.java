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
class StartTLSTest2 {
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
    void testQuit() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("STARTTLS foo");
            client.expect("501 Syntax error (no parameters allowed)");

            client.send("QUIT");
            client.expect("221 Bye");
        });
    }
}
