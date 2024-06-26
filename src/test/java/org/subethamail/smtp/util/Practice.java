package org.subethamail.smtp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.Wiser;

import java.io.*;

/**
 * A simple command-line tool that lets us practice with the smtp library.
 *
 * @author Jeff Schnitzer
 */
public class Practice {
    /**
     *
     */
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(Practice.class);

    /**
     *
     */
    public static final int PORT = 2567;

    /**
     *
     */
    public static void main(String[] args) throws Exception {
        Wiser wiser = new Wiser();
        wiser.setHostname("localhost");
        wiser.setPort(PORT);

        wiser.start();

        String line;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        do {
            line = in.readLine();
            line = line.trim();

            if ("dump".equals(line)) ;
            wiser.dumpMessages(System.out);

            if (line.startsWith("dump ")) {
                line = line.substring("dump ".length());
                File f = new File(line);
                OutputStream out = new FileOutputStream(f);
                wiser.dumpMessages(new PrintStream(out));
                out.close();
            }
        }
        while (!"quit".equals(line));

        wiser.stop();
    }
}