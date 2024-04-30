package org.subethamail.smtp.util;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.Wiser;

/**
 * A base class for testing the SMTP server at the raw protocol level.
 * Handles setting up and tearing down of the server.
 *
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public abstract class ServerTestCase
{
	/** */
	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory.getLogger(ServerTestCase.class);

	/** */
	public static final int PORT = 2572;

	/**
	 * Override the accept method in Wiser so we can test
	 * the accept method().
	 */
	public class TestWiser extends Wiser
	{
		@Override
		public boolean accept(String from, String recipient)
		{
			if (recipient.equals("failure@subethamail.org"))
			{
				return false;
			}
			else if (recipient.equals("success@subethamail.org"))
			{
				return true;
			}
			return true;
		}
	}

	/** */
	protected TestWiser wiser;

	/** */
	protected Client c;

	/** */
	public ServerTestCase(String name)
	{
	}

	/** */
	@BeforeEach
	protected void setUp() throws Exception
	{

		this.wiser = new TestWiser();
		this.wiser.setHostname("localhost");
		this.wiser.setPort(PORT);
		this.wiser.start();

		this.c = new Client("localhost", PORT);
	}

	/** */
	@AfterEach
	protected void tearDown() throws Exception
	{
		this.wiser.stop();
		this.wiser = null;

		this.c.close();

	}

	/** */
	public void send(String msg) throws Exception
	{
		this.c.send(msg);
	}

	/** */
	public void expect(String msg) throws Exception
	{
		this.c.expect(msg);
	}

	/** */
	public void expectContains(String msg) throws Exception
	{
		this.c.expectContains(msg);
	}
}