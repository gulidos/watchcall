package ru.in.watcher.sip.content;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.servlet.sip.SipServletMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.in.isp.Isup;

public class Contents {
    private static Logger logger = LoggerFactory.getLogger(Contents.class);


    public static final String CONTENT_ENCODING = "ISO-8859-1";
    public static final String SDP_ZERO =
            "v=0\r\n"
                    + "o=itscc 0 0 IN IP4 0.0.0.0\r\n"
                    + "s=its\r\n"
                    + "c=IN IP4 0.0.0.0\r\n"
                    + "t=0 0\r\n"
                    + "m=audio 1234 RTP/AVP 8\r\n"
                    + "a=inactive\r\n";


    public static final String MIME_MSCML = "application/mediaservercontrol+xml";
    public static final String MIME_SDP = "application/sdp";
    public static final String MIME_GTD = "application/gtd";
    public static final String MIME_ISUP = "application/isup";
    public static final String MIME_MULTIPART = "multipart/mixed";
    private static boolean IS_NEW_STYLE_PARSE_MULTIPART = true;
    
    
	public static String parseSdp(SipServletMessage msg) {
		String sdp = parseContent(msg, MIME_SDP);
		// sdp must end with \r\n
		if (sdp != null && !sdp.endsWith("\r\n")) {
			return sdp + "\r\n";
		}

		return sdp;
	}
	
	public static Content addSipiToSdp(Content sdp, Isup isup) throws IOException {
        try {
            String boundary = "unique-boundary-1";
            MimeMultipart multipart = new MyMimeMultipart("mixed", boundary);

            MimeBodyPart sdpPart = new MimeBodyPart();
            sdpPart.setContent(sdp.raw, sdp.type);
            sdpPart.setHeader("Content-Type", sdp.type);
            multipart.addBodyPart(sdpPart);

            // sipi part
            MimeBodyPart sipiPart = new MimeBodyPart();
            sipiPart.setContent(isup.encode(), Isup.SIP_I_Content_Type);
            sipiPart.setHeader("Content-Type", Isup.SIP_I_Content_Type);
            sipiPart.setHeader("Content-Disposition", Isup.SIP_I_Content_Disposition);
            multipart.addBodyPart(sipiPart);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            multipart.writeTo(bos);
            return new Content(bos.toByteArray(), "multipart/mixed;boundary=" + boundary);
        } catch (MessagingException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    
    public static String parseContent(SipServletMessage msg, String mime) {
        try {
            Object content = msg.getContent();
            if (content == null) {
                return null;
            }

            String contentType = msg.getContentType().toLowerCase();

//            if (logger.isDebugEnabled()) {
//                logger.debug("content=" + content.getClass() +
//                        ",type=" + contentType +
//                        ",mime=" + mime +
//                        ",charset=" + CONTENT_ENCODING);
//            }

            if (contentType.startsWith(mime)) {

                return content instanceof String ? (String) content :
                        new String((byte[]) content, CONTENT_ENCODING);
            } else if (contentType.startsWith(MIME_MULTIPART)) {

                //extract boundaryKey from Content-Type:
                //(example: Content-Type: multipart/mixed;boundary=unique-boundary-1)
                String boundaryKey = contentType
                        .substring(MIME_MULTIPART.length() + 1); // res: boundary=unique-boundary-1
                boundaryKey = boundaryKey.substring("boundary=".length()); // res: unique-boundary-1

                //multipart string
                String multipart = new String((byte[]) content, CONTENT_ENCODING);

                if (IS_NEW_STYLE_PARSE_MULTIPART) {
                    return parseMultipart(multipart, boundaryKey, mime);
                } else {
                    return parseMultipartOld(multipart, boundaryKey, mime);
                }
            } else {

                if (logger.isDebugEnabled()) {
                    logger.debug("No support for content " + contentType);
                }
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return null;
    }


    @Deprecated
    private static String parseMultipartOld(String multipartStr, String boundaryKey, String mime) {
        String[] partsArray = multipartStr.split("--" + boundaryKey + "\r\nContent-Type:");
        for (String part : partsArray) {
            part = part.trim();
            if (part.startsWith(mime)) {
                String[] sections = part.split("\r\n\r\n");
                if (sections.length > 1) {
                    //0 - headers section
                    return sections[1];//body section
                }
            }
        }
        return null;
    }


    private static String parseMultipart(String multipartStr, String boundaryKey, String mime) {

		/*
         * this regexp can not be injection (or corrupt), because we use only
		 * constants mime values (see in this class MIME_SDP, MIME_GTD, ...)
		 */
        Pattern contentTypePattern = Pattern.compile("^Content-Type:\\s*" + mime.toLowerCase() + ".*\\s*$",
                Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

        String expectedHead = "--" + boundaryKey + "\r\n";
        String expectedTail = "\r\n--" + boundaryKey + "--";
        String expectedDelimeter = "\r\n--" + boundaryKey + "\r\n";

        if (multipartStr.startsWith(expectedHead) && multipartStr.endsWith(expectedTail)) {
            String cleanMultipart = multipartStr;
            //1. cleaning: cut head and tail
            cleanMultipart = cleanMultipart.substring(expectedHead.length()); //cut head
            cleanMultipart = cleanMultipart.substring(0, cleanMultipart.length() - expectedTail.length()); //cut tail
            //2. split on many parts by delimiter
            String[] partsArray = cleanMultipart.split(expectedDelimeter);
            for (String eachPart : partsArray) {
                //3. split each part on header and body
                String headerAndBody[] = eachPart.split("\r\n\r\n", 2);
                if (headerAndBody.length == 2) {
                    String headerPart = headerAndBody[0];
                    String bodyPart = headerAndBody[1];
                    Matcher matcher = contentTypePattern.matcher(headerPart);
                    //4. find part by the specified mime
                    if (matcher.find()) {
                        return bodyPart;
                    }
                }
            }
        }
        return null;
    }

}
