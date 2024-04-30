package org.subethamail.smtp.command;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.Session;

import java.io.IOException;

/**
 *
 * @author Michele Zuccala < zuccala.m@gmail.com >
 */
public class ExpandCommand extends BaseCommand {
    /** */
    public ExpandCommand() {
        super("EXPN", "The expn command.");
    }

    /** */
    @Override
    public void execute(String commandString, Session sess) throws IOException {
        sess.sendResponse("502 EXPN command is disabled");
    }
}
