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
public class DataTest2 {
    static Wiser wiser;
    static Client client;


    @BeforeAll
    @SneakyThrows
    static void init() {
        wiser = TestWiser.init();

        wiser.start();
    }

    @Test
    void testNeedMail() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);

            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("DATA");
            client.expect("503 5.5.1 Error: need MAIL command");
        });
    }

    /**
     *
     */
    @Test
    void testNeedRcpt() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("MAIL FROM: success@subethamail.org");
            client.expect("250");

            client.send("DATA");
            client.expect("503 Error: need RCPT command");
        });
    }

    /**
     *
     */
    @Test
    void testData() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("MAIL FROM: success@subethamail.org");
            client.expect("250");

            client.send("RCPT TO: success@subethamail.org");
            client.expect("250");

            client.send("DATA");
            client.expect("354 End data with <CR><LF>.<CR><LF>");
        });
    }

    /**
     *
     */
    @Test
    void testRsetAfterData() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
            client.expect("220");

            client.send("HELO foo.com");
            client.expect("250");

            client.send("MAIL FROM: success@subethamail.org");
            client.expect("250");

            client.send("RCPT TO: success@subethamail.org");
            client.expect("250");

            client.send("DATA");
            client.expect("354 End data with <CR><LF>.<CR><LF>");

            client.send("alsdkfj \r\n.");

            client.send("RSET");
            client.expect("250 Ok");

            client.send("HELO foo.com");
            client.expect("250");
        });
    }
}
