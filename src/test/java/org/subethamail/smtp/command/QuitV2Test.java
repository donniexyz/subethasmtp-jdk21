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
class QuitV2Test {
    static Wiser wiser;
    static Client client;
    static int port = TestWiser.PORT + 6;


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
    void testQuit() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", port);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("MAIL FROM: test@example.com");
            client.expect("250 Ok");

            client.send("QUIT");
            client.expect("221 Bye");
        });
    }
}
