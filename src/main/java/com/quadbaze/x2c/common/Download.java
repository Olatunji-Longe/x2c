package com.quadbaze.x2c.common;

import tech.blueglacier.email.Attachment;

import java.util.List;

/**
 * Created by Olatunji O. Longe on 05/05/2018 10:10 PM
 */
public class Download {
    private String key;
    private List<Attachment> attachments;

    public Download(String key, List<Attachment> attachments) {
        this.key = key;
        this.attachments = attachments;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}
