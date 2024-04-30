package org.subethamail.smtp.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class PlainAuthenticatorTest {

    @Mock
    private SmartClient smartClient;

    private final Map<String, String> extensions = new HashMap<String, String>();

    @Test
    public void testSuccess() throws IOException {
        extensions.put("AUTH", "GSSAPI DIGEST-MD5 PLAIN");
        PlainAuthenticator authenticator = new PlainAuthenticator(smartClient,
                "test", "1234");

        Mockito.when(smartClient.getExtensions()).thenReturn(extensions);
        authenticator.authenticate();

        Mockito.verify(smartClient).sendAndCheck("AUTH PLAIN AHRlc3QAMTIzNA==");

    }

}
