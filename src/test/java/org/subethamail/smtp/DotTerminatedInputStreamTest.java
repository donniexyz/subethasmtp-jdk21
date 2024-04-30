package org.subethamail.smtp;

import org.junit.jupiter.api.Test;
import org.subethamail.smtp.io.DotTerminatedInputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DotTerminatedInputStreamTest {
    @Test
    void testEmpty() throws IOException {
        InputStream in = new ByteArrayInputStream(".\r\n".getBytes(StandardCharsets.US_ASCII));
        DotTerminatedInputStream stream = new DotTerminatedInputStream(in);
        assertEquals(-1, stream.read());
    }

    @Test
    void testPreserveLastCrLf() throws IOException {
        InputStream in = new ByteArrayInputStream("a\r\n.\r\n".getBytes(StandardCharsets.US_ASCII));
        DotTerminatedInputStream stream = new DotTerminatedInputStream(in);
        assertEquals("a\r\n", readFull(stream));
    }

    @Test
    void testDotDot() throws IOException {
        InputStream in = new ByteArrayInputStream("..\r\n.\r\n".getBytes(StandardCharsets.US_ASCII));
        DotTerminatedInputStream stream = new DotTerminatedInputStream(in);
        assertEquals("..\r\n", readFull(stream));
    }

    @Test
    void testMissingDotLine() {
        InputStream in = new ByteArrayInputStream("a\r\n".getBytes(StandardCharsets.US_ASCII));
        DotTerminatedInputStream stream = new DotTerminatedInputStream(in);
        assertThrows(EOFException.class, () -> readFull(stream));
    }

    private String readFull(DotTerminatedInputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int ch;
        while (-1 != (ch = in.read()))
            out.write(ch);
        return out.toString(StandardCharsets.US_ASCII);
    }
}
