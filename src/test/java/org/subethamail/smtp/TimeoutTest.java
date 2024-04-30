package org.subethamail.smtp;

import org.junit.jupiter.api.Test;
import org.subethamail.smtp.client.SMTPClient;
import org.subethamail.wiser.Wiser;

import java.net.SocketException;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * This class tests connection timeouts.
 *
 * @author Jeff Schnitzer
 * @author Dony Zulkarnaen
 */
class TimeoutTest {
    /**
     *
     */
    public static final int PORT = 2570;

    /**
     *
     */
    @Test
    void testTimeout() throws Exception {
        Wiser wiser = new Wiser();
        wiser.setPort(PORT);
        wiser.getServer().setConnectionTimeout(1000);
        wiser.start();

        SMTPClient client = new SMTPClient("localhost", PORT);
        client.sendReceive("HELO foo");
        Thread.sleep(2000);
        try {
            client.sendReceive("HELO bar");
            fail("sendReceive() went thru after exceeding timeout");
        } catch (SocketException e) {
            // expected
        } finally {
            wiser.stop();
        }
    }

}
