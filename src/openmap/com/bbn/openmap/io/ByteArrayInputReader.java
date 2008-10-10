package com.bbn.openmap.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * An {@link InputReader} working on a byte array
 * 
 * @author halset
 */
public class ByteArrayInputReader extends StreamInputReader {

    private byte[] bytes;

    public ByteArrayInputReader(byte[] bytes) {
        this.bytes = bytes;
        try {
            reopen();
        } catch (IOException e) {
            // should not happen
            throw new RuntimeException(e);
        }
    }

    protected void reopen() throws IOException {
        super.reopen();

        inputStream = new ByteArrayInputStream(bytes);
    }

}
