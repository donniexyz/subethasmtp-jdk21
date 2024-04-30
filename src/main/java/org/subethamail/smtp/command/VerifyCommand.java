package org.subethamail.smtp.command;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.Session;

import java.io.IOException;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
public class VerifyCommand extends BaseCommand {
    /** */
    public VerifyCommand() {
        super("VRFY", "The vrfy command.");
    }

    /** */
    @Override
    public void execute(String commandString, Session sess) throws IOException {
        sess.sendResponse("502 VRFY command is disabled");
    }
}
