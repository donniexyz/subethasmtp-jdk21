package org.subethamail.smtp.command;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.Session;

import java.io.IOException;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class ResetCommand extends BaseCommand {
    /** */
    public ResetCommand() {
        super("RSET", "Resets the system.");
    }

    /** */
    @Override
    public void execute(String commandString, Session sess) throws IOException {
        sess.resetMailTransaction();

        sess.sendResponse("250 Ok");
    }
}
