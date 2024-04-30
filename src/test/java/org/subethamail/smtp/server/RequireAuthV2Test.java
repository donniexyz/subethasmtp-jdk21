package org.subethamail.smtp.server;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.subethamail.smtp.auth.EasyAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.helper.TestWiser;
import org.subethamail.smtp.util.Base64;
import org.subethamail.smtp.util.Client;
import org.subethamail.smtp.util.TextUtils;
import org.subethamail.wiser.Wiser;

/**
 * @author Dony Zulkarnaen
 */
class RequireAuthV2Test {
    static final String REQUIRED_USERNAME = "myUserName";
    static final String REQUIRED_PASSWORD = "mySecret01";

    static class RequiredUsernamePasswordValidator implements UsernamePasswordValidator {
        public void login(String username, String password) throws LoginFailedException {
            if (!username.equals(REQUIRED_USERNAME) || !password.equals(REQUIRED_PASSWORD)) {
                throw new LoginFailedException();
            }
        }
    }

    static Wiser wiser;
    static Client client;
    static int port = TestWiser.PORT + 9;


    @BeforeAll
    @SneakyThrows
    static void init() {
        wiser = TestWiser.init(port);

        UsernamePasswordValidator validator = new RequiredUsernamePasswordValidator();

        EasyAuthenticationHandlerFactory fact = new EasyAuthenticationHandlerFactory(validator);
        wiser.getServer().setAuthenticationHandlerFactory(fact);
        wiser.getServer().setRequireAuth(true);
        wiser.start();
    }


    /**
     *
     */
    @Test
    void testAuthRequired() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", port);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("EHLO foo.com");
            client.expect("250");

            client.send("NOOP");
            client.expect("250");

            client.send("RSET");
            client.expect("250");

            client.send("MAIL FROM: test@example.com");
            client.expect("530 5.7.0  Authentication required");

            client.send("RCPT TO: test@example.com");
            client.expect("530 5.7.0  Authentication required");

            client.send("DATA");
            client.expect("530 5.7.0  Authentication required");

            client.send("STARTTLS");
            client.expect("454 TLS not supported");

            client.send("QUIT");
            client.expect("221 Bye");
        });
    }

    /**
     *
     */
    @Test
    void testAuthSuccess() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", port);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("AUTH LOGIN");
            client.expect("334");

            String enc_username = Base64.encodeToString(TextUtils.getAsciiBytes(REQUIRED_USERNAME), false);

            client.send(enc_username);
            client.expect("334");

            String enc_pwd = Base64.encodeToString(TextUtils.getAsciiBytes(REQUIRED_PASSWORD), false);
            client.send(enc_pwd);
            client.expect("235");

            client.send("MAIL FROM: test@example.com");
            client.expect("250");

            client.send("RCPT TO: test@example.com");
            client.expect("250");

            client.send("DATA");
            client.expect("354");

            client.send("\r\n.");
            client.expect("250");

            client.send("QUIT");
            client.expect("221 Bye");
        });
    }
}
