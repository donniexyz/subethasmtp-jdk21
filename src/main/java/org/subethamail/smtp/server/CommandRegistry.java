/*
 * Commands.java Created on November 18, 2006, 12:26 PM To change this template,
 * choose Tools | Template Manager and open the template in the editor.
 */

package org.subethamail.smtp.server;

import lombok.Getter;
import org.subethamail.smtp.command.*;

import java.util.List;
import java.util.Locale;

/**
 * Enumerates all the Commands made available in this release.
 *
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 * @author Dony Zulkarnaen
 */
@Getter
public enum CommandRegistry {
    AUTH(List.of("AUTH"), new AuthCommand(), true, false),
    DATA(List.of("DATA"), new DataCommand(), true, true),
    EHLO(List.of("EHLO"), new EhloCommand(), false, false),
    HELO(List.of("HELO"), new HelloCommand(), true, false),
    HELP(List.of("HELP"), new HelpCommand(), true, true),
    MAIL(List.of("MAIL FROM", "MAIL"), new MailCommand(), true, true),
    NOOP(List.of("NOOP"), new NoopCommand(), false, false),
    QUIT(List.of("QUIT"), new QuitCommand(), false, false),
    RCPT(List.of("RCPT TO", "RCPT"), new ReceiptCommand(), true, true),
    RSET(List.of("RSET"), new ResetCommand(), true, false),
    STARTTLS(List.of("STARTTLS"), new StartTLSCommand(), false, false),
    VRFY(List.of("VRFY"), new VerifyCommand(), true, true),
    EXPN(List.of("EXPN"), new ExpandCommand(), true, true);

    /**
     *
     */
    private Command command;
    private List<String> cmdKeys;

    /**
     *
     */
    CommandRegistry(List<String> cmdKeys, Command cmd, boolean checkForStartedTLSWhenRequired, boolean checkForAuthIfRequired) {
        if (checkForStartedTLSWhenRequired)
            this.command = new RequireTLSCommandWrapper(cmd);
        else
            this.command = cmd;
        if (checkForAuthIfRequired)
            this.command = new RequireAuthCommandWrapper(this.command);
        this.cmdKeys = cmdKeys;
    }

    public static String[] split(String fullCommand) {
        String up = fullCommand.toUpperCase(Locale.ENGLISH);
        for (CommandRegistry value : CommandRegistry.values()) {
            for (String cmd : value.cmdKeys) {
                if (up.startsWith(cmd)) {
                    if (cmd.length() >= fullCommand.length())
                        return new String[]{cmd, ""};
                    if (" \t\n\r\f".contains("" + up.charAt(cmd.length()))) {
                        return new String[]{cmd, fullCommand.substring(cmd.length() + 1)};
                    }
                }
            }
        }
        return null;
    }
}
