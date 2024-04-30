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
public class CommandTest2 {

    static Wiser wiser;
    static Client client;


    @BeforeAll
    @SneakyThrows
    static void init() {
        wiser = TestWiser.init();

        wiser.start();
    }

    @Test
    public void testCommandHandling() {
        Assertions.assertDoesNotThrow(() -> {
            client = new Client("localhost", TestWiser.PORT);
            client.expect("220");

            client.send("blah blah blah");
            client.expect("500 Error: command not implemented");

        });
    }
}
