package org.subethamail.smtp.command;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.Session;

import java.io.IOException;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class QuitCommand extends BaseCommand {
    /** */
    public QuitCommand() {
        super("QUIT", "Exit the SMTP session.");
    }

    /** */
    @Override
    public void execute(String commandString, Session sess) throws IOException {
        sess.sendResponse("221 Bye");
        sess.quit();
    }
}
