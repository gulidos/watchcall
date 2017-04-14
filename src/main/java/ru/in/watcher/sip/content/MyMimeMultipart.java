package ru.in.watcher.sip.content;

import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMultipart;

/**
 * Create MimeMultipart with my boundary
 */
public class MyMimeMultipart extends MimeMultipart {

    public MyMimeMultipart(String subtype, String boundary) {
        super();
        ContentType cType = new ContentType("multipart", subtype, null);
        cType.setParameter("boundary", boundary);
        contentType = cType.toString();
    }
}