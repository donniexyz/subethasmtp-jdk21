package org.subethamail.smtp.server;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;
import org.subethamail.smtp.util.TextUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * This class tests whether the event handler methods defined in MessageHandler
 * are called at the appropriate times and in good order.
 *
 * @author Dony Zulkarnaen
 */
@ExtendWith(MockitoExtension.class)
class MessageHandlerV2Test {
    @Mock
    static private MessageHandlerFactory messageHandlerFactory;

    @Mock
    private MessageHandler messageHandler;

    @Mock
    private MessageHandler messageHandler2;

    private SMTPServer smtpServer;

    @BeforeEach
    void setup() {
        smtpServer = new SMTPServer(messageHandlerFactory);
        smtpServer.setPort(2566);
        smtpServer.start();
    }

    @Test
    @SneakyThrows
    void testCompletedMailTransaction() {

        Mockito.when(messageHandlerFactory.create(any())).thenReturn(messageHandler);

        SmartClient client = new SmartClient("localhost", smtpServer.getPort(),
                "localhost");
        client.from("john@example.com");
        client.to("jane@example.com");
        client.dataStart();
        client.dataWrite(TextUtils.getAsciiBytes("body"), 4);
        client.dataEnd();
        client.quit();
        smtpServer.stop(); // wait for the server to catch up

        Mockito.verify(messageHandler).from(anyString());
        Mockito.verify(messageHandler).recipient(anyString());
        Mockito.verify(messageHandler).data(any(InputStream.class));
        Mockito.verify(messageHandler).done();
    }

    @Test
    @SneakyThrows
    void testDisconnectImmediately() {

        SmartClient client = new SmartClient("localhost", smtpServer.getPort(),
                "localhost");
        client.quit();
        smtpServer.stop(); // wait for the server to catch up
        Mockito.verifyNoInteractions(messageHandler);
    }

    @Test
    @SneakyThrows
    void testAbortedMailTransaction() {

        Mockito.when(messageHandlerFactory.create(any())).thenReturn(messageHandler);

        SmartClient client = new SmartClient("localhost", smtpServer.getPort(),
                "localhost");
        client.from("john@example.com");
        client.quit();
        smtpServer.stop(); // wait for the server to catch up

        Mockito.verify(messageHandler).from(anyString());
        Mockito.verify(messageHandler).done();

    }

    @Test
    @SneakyThrows
    void testTwoMailsInOneSession() {

        Mockito.when(messageHandlerFactory.create(any())).thenReturn(messageHandler);

        SmartClient client = new SmartClient("localhost", smtpServer.getPort(),
                "localhost");

        client.from("john1@example.com");
        client.to("jane1@example.com");
        client.dataStart();
        client.dataWrite(TextUtils.getAsciiBytes("body1"), 5);
        client.dataEnd();

        Mockito.verify(messageHandler).from(anyString());
        Mockito.verify(messageHandler).recipient(anyString());
        Mockito.verify(messageHandler).data(any(InputStream.class));
        Mockito.verify(messageHandler).done();

        Mockito.when(messageHandlerFactory.create(any())).thenReturn(messageHandler2);

        client.from("john2@example.com");
        client.to("jane2@example.com");
        client.dataStart();
        client.dataWrite(TextUtils.getAsciiBytes("body2"), 5);
        client.dataEnd();

        client.quit();

        smtpServer.stop(); // wait for the server to catch up

        Mockito.verify(messageHandler2).from(anyString());
        Mockito.verify(messageHandler2).recipient(anyString());
        Mockito.verify(messageHandler2).data(any(InputStream.class));
        Mockito.verify(messageHandler2).done();

    }

    /**
     * Test for issue 56: rejecting a Mail From causes IllegalStateException in
     * the next Mail From attempt.
     *
     * @see <a href=http://code.google.com/p/subethasmtp/issues/detail?id=56>Issue 56</a>
     */
    @Test
    void testMailFromRejectedFirst() throws IOException {
        Mockito.when(messageHandlerFactory.create(any())).thenReturn(messageHandler);

        SmartClient client = new SmartClient("localhost", smtpServer.getPort(),
                "localhost");

        Mockito.doThrow(new RejectException("Test MAIL FROM rejection"))
                .when(messageHandler).from(anyString());

        Assertions.assertThrows(SMTPException.class, () -> client.from("john1@example.com"));

        Mockito.when(messageHandlerFactory.create(any())).thenReturn(messageHandler2);

        client.from("john2@example.com");
        client.quit();

        smtpServer.stop(); // wait for the server to catch up

        Mockito.verify(messageHandler2).from(anyString());
        Mockito.verify(messageHandler2).done();
    }

}
