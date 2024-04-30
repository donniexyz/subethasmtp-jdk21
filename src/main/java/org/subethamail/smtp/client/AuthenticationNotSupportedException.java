package org.subethamail.smtp.client;

import java.io.IOException;
import java.io.Serial;

/**
 * Indicates that the server either does not support authentication at all or no
 * authentication mechanism exists which is supported by both the server and the
 * client.
 */
public class AuthenticationNotSupportedException extends IOException {
    @Serial
	private static final long serialVersionUID = 4269158574227243089L;

    public AuthenticationNotSupportedException(String message) {
        super(message);
    }
}
