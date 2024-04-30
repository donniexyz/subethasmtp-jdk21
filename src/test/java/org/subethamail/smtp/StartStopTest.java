package org.subethamail.smtp;

import jakarta.mail.Session;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.Wiser;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class attempts to quickly start/stop 10 Wiser servers. It makes sure that the socket bind address is correctly
 * shut down.
 *
 * @author Jon Stevens
 * @author Dony Zulkarnaen
 */
class StartStopTest {
    /**
     *
     */
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(StartStopTest.class);

    /**
     *
     */
    public static final int PORT = 2569;

    /**
     *
     */
    protected Session session;

    protected int counter = 0;


    /**
     *
     */
    @BeforeEach
    protected void setUp() {

        Properties props = new Properties();
        props.setProperty("mail.smtp.host", "localhost");
        props.setProperty("mail.smtp.port", Integer.toString(PORT));
        this.session = Session.getDefaultInstance(props);
    }


    /**
     *
     */
    @Test
    void testMultipleStartStop() {
        for (int i = 0; i < 10; i++) {
            this.startStop(i > 5);
        }
        assertEquals(10, this.counter);
    }

    /**
     *
     */
    @SneakyThrows
    private void startStop(boolean pause) {
        Wiser wiser = new Wiser();
        wiser.setPort(PORT);

        wiser.start();

        if (pause)
            Thread.sleep(1000);

        wiser.stop();

        this.counter++;
    }

}
