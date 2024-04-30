package org.subethamail.smtp;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.io.ReceivedHeaderStream;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests a bug in ReceivedHeaderStream which
 * has since been fixed.
 *
 * @see <a href="http://www.subethamail.org/se/archive_msg.jsp?msgId=59719">http://www.subethamail.org/se/archive_msg.jsp?msgId=59719</a>
 */
public class ReceivedHeaderStreamTest
{
	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory.getLogger(ReceivedHeaderStreamTest.class);


	/** */
	@Test
	public void testReceivedHeader() throws Exception
	{
		int BUF_SIZE = 10000;
		int offset = 10;
		ByteArrayInputStream in = new ByteArrayInputStream("hello world".getBytes());
		ReceivedHeaderStream hdrIS = new ReceivedHeaderStream(in, "ehlo",
				InetAddress.getLocalHost(), "foo", null, "123", null);
		byte[] buf = new byte[BUF_SIZE];
		int len = hdrIS.read(buf, offset, BUF_SIZE-offset);

		String result = new String(buf, offset, len);

		assertTrue(result.endsWith("\nhello world"));
	}
}