package org.subethamail.smtp.client;

import lombok.Getter;
import org.subethamail.smtp.client.SMTPClient.Response;

import java.io.IOException;

/**
 * Thrown if a syntactically valid reply was received from the server, which
 * indicates an error via the status code.
 */
@Getter
public class SMTPException extends IOException {
    Response response;

    public SMTPException(Response resp) {
        super(resp.toString());

        this.response = resp;
    }

}
