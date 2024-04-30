package org.subethamail.smtp.helper;

import org.subethamail.wiser.Wiser;

/**
 * @author Dony Zulkarnaen
 */
public class TestWiser extends Wiser {

    public static final int PORT = 2571;
    public static final String LOCALHOST = "localhost";

    public static TestWiser init() {

        TestWiser wiser;
        wiser = new TestWiser();
        wiser.setHostname(LOCALHOST);
        wiser.setPort(PORT);

        return wiser;
    }


    @Override
    public boolean accept(String from, String recipient) {
        if (recipient.equals("failure@subethamail.org")) {
            return false;
        } else if (recipient.equals("success@subethamail.org")) {
            return true;
        }
        return true;
    }
}
