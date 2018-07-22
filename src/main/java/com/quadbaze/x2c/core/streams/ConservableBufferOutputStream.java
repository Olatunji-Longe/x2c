package com.quadbaze.x2c.core.streams;

import java.io.ByteArrayOutputStream;

/**
 * Created by Olatunji O. Longe on 05/05/2018 2:45 PM
 */
public class ConservableBufferOutputStream extends ByteArrayOutputStream {

    public UploadableInputStream toInputStream(){
        // Creates input stream by using current buffer and without re-copying the buffer (hence avoiding eating up of memory)
        return new UploadableInputStream(buf, 0, count);
    }
}
