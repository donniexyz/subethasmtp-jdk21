package org.subethamail.smtp.server;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.helper.TestWiser;
import org.subethamail.smtp.util.Client;
import org.subethamail.wiser.Wiser;

/**
 * @author Dony Zulkarnaen
 */
class RequireTlsV2Test {

    static Wiser wiser;
    static Client client;
    static int port = TestWiser.PORT + 10;


    @BeforeAll
    @SneakyThrows
    static void init() {
        wiser = TestWiser.init(port);

        UsernamePasswordValidator validator = new RequireAuthV2Test.RequiredUsernamePasswordValidator();
        wiser.getServer().setRequireTLS(true);
        wiser.start();
    }


    /**
     *
     */
    @Test
    void testNeedSTARTTLS() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", port);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("530 Must issue a STARTTLS command first");

            client.send("EHLO foo.com");
            client.expect("250");

            client.send("NOOP");
            client.expect("250");

            client.send("MAIL FROM: test@example.com");
            client.expect("530 Must issue a STARTTLS command first");

            client.send("STARTTLS foo");
            client.expect("501 Syntax error (no parameters allowed)");

            client.send("QUIT");
            client.expect("221 Bye");
        });
    }

}