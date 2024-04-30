package org.subethamail.smtp.server;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.subethamail.smtp.auth.EasyAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.helper.TestWiser;
import org.subethamail.smtp.util.Client;
import org.subethamail.wiser.Wiser;

/**
 * @author Dony Zulkarnaen
 */
public class RequireTlsTest2 {

    static Wiser wiser;
    static Client client;


    @BeforeAll
    @SneakyThrows
    static void init() {
        wiser = TestWiser.init();

        UsernamePasswordValidator validator = new RequireAuthTest2.RequiredUsernamePasswordValidator();

        EasyAuthenticationHandlerFactory fact = new EasyAuthenticationHandlerFactory(validator);
        wiser.getServer().setAuthenticationHandlerFactory(fact);
        wiser.getServer().setRequireAuth(true);
        wiser.start();
    }


    /**
     *
     */
    @Test
    public void testNeedSTARTTLS() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
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