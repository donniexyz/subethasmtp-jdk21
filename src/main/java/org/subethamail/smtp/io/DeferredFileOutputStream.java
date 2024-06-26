/*
 * $Id$
 * $URL$
 */
package org.subethamail.smtp.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * This works like a ByteArrayOutputStream until a certain size is
 * reached, then creates a temp file and acts like a buffered
 * FileOutputStream.  The data can be retrieved afterward by
 * calling getInputStream().
 * <p>
 * When this object is closed, the temporary file is deleted.  You
 * can no longer call getInputStream().
 *
 * @author Jeff Schnitzer
 */

public class DeferredFileOutputStream extends ThresholdingOutputStream {
    private static final Logger log = LoggerFactory.getLogger(DeferredFileOutputStream.class);

    /**
     * Initial size of the byte array buffer.  Better to make this
     * large to start with so that we can avoid reallocs; mail
     * messages are rarely tiny.
     */
    static final int INITIAL_BUF_SIZE = 8192;

    /** */
    public static final String TMPFILE_PREFIX = "subetha";
    public static final String TMPFILE_SUFFIX = ".msg";

    /** If we switch to file output, this is the file. */
    File outFile;

    /** If we switch to file output, this is the stream. */
    FileOutputStream outFileStream;

    /** When the output stream is closed, this becomes true */
    boolean closed;

    boolean thresholdReached = false;

    /**
     * @param transitionSize is the number of bytes at which to convert
     *  from a byte array to a real file.
     */
    public DeferredFileOutputStream(int transitionSize) {
        super(new BetterByteArrayOutputStream(INITIAL_BUF_SIZE), transitionSize);
    }

    /*
     * (non-Javadoc)
     * @see org.subethamail.common.io.ThresholdingOutputStream#thresholdReached(int, int)
     */
    @Override
    protected void thresholdReached(int current, int predicted) throws IOException {
        // Open a temp file, write the byte array version, and swap the
        // output stream to the file version.

        this.outFile = File.createTempFile(TMPFILE_PREFIX, TMPFILE_SUFFIX);
        this.outFileStream = new FileOutputStream(this.outFile);

        ((ByteArrayOutputStream) this.output).writeTo(this.outFileStream);
        this.output = new BufferedOutputStream(this.outFileStream);
    }

    /**
     * Closes the output stream and creates an InputStream on the same data.
     *
     * @return either a BetterByteArrayOutputStream or buffered FileInputStream,
     *  depending on what state we are in.
     */
    public InputStream getInputStream() throws IOException {
        if (this.output instanceof BetterByteArrayOutputStream better) {
            return better.getInputStream();
        } else {
            if (!this.closed) {
                this.output.flush();
                this.output.close();
                this.closed = true;
            }

            return new BufferedInputStream(new FileInputStream(this.outFile));
        }
    }

    /* (non-Javadoc)
     * @see org.subethamail.common.io.ThresholdingOutputStream#close()
     */
    @Override
    public void close() throws IOException {
        if (!this.closed) {
            this.output.flush();
            this.output.close();
            this.closed = true;
        }

        if (this.outFile != null) {
            boolean deleted = this.outFile.delete();
            log.info("File {} deletion result {}", outFile.getName(), deleted);
        }
    }

}
