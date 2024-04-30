package org.subethamail.smtp.command;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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
@ExtendWith(MockitoExtension.class)
public class AuthTest2 {

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


    @BeforeAll
    @SneakyThrows
    static void init() {
        wiser = TestWiser.init();

        wiser.start();

        UsernamePasswordValidator validator = new RequiredUsernamePasswordValidator();

        EasyAuthenticationHandlerFactory fact = new EasyAuthenticationHandlerFactory(validator);
        wiser.getServer().setAuthenticationHandlerFactory(fact);

    }


    /**
     * Test method for AUTH PLAIN.
     * The sequence under test is as follows:
     * <ol>
     * <li>HELO test</li>
     * <li>User starts AUTH PLAIN</li>
     * <li>User sends username+password</li>
     * <li>We expect login to be successful. Also the Base64 transformations are tested.</li>
     * <li>User issues another AUTH command</li>
     * <li>We expect an error message</li>
     * </ol>
     * {@link AuthCommand#execute(String, org.subethamail.smtp.server.Session)}.
     */
    @Test
    void testAuthPlain() {

        Assertions.assertDoesNotThrow(() -> {

            client = new Client("localhost", TestWiser.PORT);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("AUTH PLAIN");
            client.expect("334");

            String authString = new String(new byte[]{0}) + REQUIRED_USERNAME
                    + new String(new byte[]{0}) + REQUIRED_PASSWORD;

            String enc_authString = Base64.encodeToString(TextUtils.getAsciiBytes(authString), false);
            client.send(enc_authString);
            client.expect("235");

            client.send("AUTH");
            client.expect("503");

        });
    }

    /**
     * Test method for AUTH LOGIN.
     * The sequence under test is as follows:
     * <ol>
     * <li>HELO test</li>
     * <li>User starts AUTH LOGIN</li>
     * <li>User sends username</li>
     * <li>User cancels authentication by sending "*"</li>
     * <li>User restarts AUTH LOGIN</li>
     * <li>User sends username</li>
     * <li>User sends password</li>
     * <li>We expect login to be successful. Also the Base64 transformations are tested.</li>
     * <li>User issues another AUTH command</li>
     * <li>We expect an error message</li>
     * </ol>
     * {@link org.subethamail.smtp.command.AuthCommand#execute(java.lang.String, org.subethamail.smtp.server.Session)}.
     */
    @Test
    public void testAuthLogin() {

        Assertions.assertDoesNotThrow(() -> {

            client = new Client("localhost", TestWiser.PORT);

            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("AUTH LOGIN");
            client.expect("334");

            String enc_username = Base64.encodeToString(TextUtils.getAsciiBytes(REQUIRED_USERNAME), false);

            client.send(enc_username);
            client.expect("334");

            client.send("*");
            client.expect("501");

            client.send("AUTH LOGIN");
            client.expect("334");

            client.send(enc_username);
            client.expect("334");

            String enc_pwd = Base64.encodeToString(TextUtils.getAsciiBytes(REQUIRED_PASSWORD), false);
            client.send(enc_pwd);
            client.expect("235");

            client.send("AUTH");
            client.expect("503");
        });
    }

    @Test
    void testMailBeforeAuth() {
        Assertions.assertDoesNotThrow(() -> {

            client = new Client("localhost", TestWiser.PORT);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("MAIL FROM: <john@example.com>");
            client.expect("250");
        });
    }

}