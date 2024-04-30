package org.subethamail.smtp;

import org.junit.jupiter.api.Test;
import org.subethamail.smtp.io.DotTerminatedOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class DotTerminatedOutputStreamTest {
    @Test
    void testEmpty() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DotTerminatedOutputStream stream = new DotTerminatedOutputStream(out);
        stream.writeTerminatingSequence();
        assertArrayEquals(".\r\n".getBytes(StandardCharsets.US_ASCII), out.toByteArray());
    }

    @Test
    void testMissingCrLf() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DotTerminatedOutputStream stream = new DotTerminatedOutputStream(out);
        stream.write('a');
        stream.writeTerminatingSequence();
        assertArrayEquals("a\r\n.\r\n".getBytes(StandardCharsets.US_ASCII), out.toByteArray());
    }

    @Test
    void testMissingCrLfByteArray() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DotTerminatedOutputStream stream = new DotTerminatedOutputStream(out);
        stream.write(new byte[]{
                'a'
        });
        stream.writeTerminatingSequence();
        assertArrayEquals("a\r\n.\r\n".getBytes(StandardCharsets.US_ASCII), out.toByteArray());
    }

    @Test
    void testExistingCrLf() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DotTerminatedOutputStream stream = new DotTerminatedOutputStream(out);
        stream.write('a');
        stream.write('\r');
        stream.write('\n');
        stream.writeTerminatingSequence();
        assertArrayEquals("a\r\n.\r\n".getBytes(StandardCharsets.US_ASCII), out.toByteArray());
    }

    @Test
    void testExistingCrLfByteArray() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DotTerminatedOutputStream stream = new DotTerminatedOutputStream(out);
        stream.write(new byte[]{
                'a', '\r', '\n'
        });
        stream.writeTerminatingSequence();
        assertArrayEquals("a\r\n.\r\n".getBytes(StandardCharsets.US_ASCII), out.toByteArray());
    }
}
