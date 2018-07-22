package com.quadbaze.x2c.core.streams;

import java.io.ByteArrayInputStream;

/**
 * Created by Olatunji O. Longe on 05/05/2018 3:53 PM
 */
public class UploadableInputStream extends ByteArrayInputStream {

    public UploadableInputStream(byte[] buf, int offset, int length) {
        super(buf, offset, length);
    }

    public final int size() {
        return super.count;
    }
}
