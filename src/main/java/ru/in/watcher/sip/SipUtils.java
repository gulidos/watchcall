package ru.in.watcher.sip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.UAMode;
import javax.servlet.sip.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.in.isp.RLC;
import ru.in.watcher.conf.Settings;
import ru.in.watcher.sip.content.IsupUtils;

public final class SipUtils {

    private static final Logger logger = LoggerFactory.getLogger(SipUtils.class);
    private static SipFactory sipFactory;
    private static TimerService timerservice;

    private SipUtils() {
        throw new AssertionError("Constant utility class, it can not instantiate");
    }

    public static SipFactory getSipFactory() {
        return sipFactory;
    }

    public static void setSipFactory(SipFactory factory) {
        sipFactory = factory;
    }
    
    public static TimerService getTimerservice() {
		return timerservice;
	}

	public static void setTimerservice(TimerService timerservice) {
		SipUtils.timerservice = timerservice;
	}

	public static SipServletResponse createRejectResponseOn(SipServletRequest req) {
        return req.createResponse(Settings.SIP_REJECT_CODE, Settings.SIP_REJECT_MSG);
    }


    public static SipServletResponse createUnavailableResponseOn(SipServletRequest req, int code) {
        // 487  SC_REQUEST_TERMINATED 
        return req.createResponse(code);
    }

    
    public static String extractUserToSipAddr(SipServletRequest req) {
        Address addr = req.getTo();
        URI uri = addr.getURI();
        String user = req.getRemoteUser();
        if (uri.isSipURI()) {
            user = ((SipURI) uri).getUser();
        }
        return user;
    }


	public static Header getHeader(SipServletRequest req, String headerName) throws ServletParseException {
		Map<String, String> map = new HashMap<String, String>();
		Address user = req.getAddressHeader(headerName);
		if (user == null) 
			throw new ServletParseException("Header " + headerName + " doesn't exist in message " + req.toString());
		
		SipURI sipUser = (SipURI) user.getURI();

		Iterator<String> i = user.getParameterNames();
		while(i.hasNext()) {
			String p = i.next();
			map.put(p, user.getParameter(p));
		}
		
		return new Header(sipUser, map);
	}

    
    public static SipServletResponse createB2bResponse(SipServletResponse resp) throws IOException, ServletParseException {
		B2buaHelper b2b = resp.getRequest().getB2buaHelper();
		SipSession linked = b2b.getLinkedSession(resp.getSession());
		SipServletResponse otherResp = 
				b2b.createResponseToOriginalRequest(linked, resp.getStatus(), resp.getReasonPhrase());
		copyContent(resp, otherResp);
		return otherResp;
	}
    
    
    public static SipServletRequest createB2bRequest(SipServletRequest incReq, Map<String, List<String>> headers) throws Exception {
		B2buaHelper helper = incReq.getB2buaHelper();
		SipServletRequest forkreq = helper.createRequest(incReq, true, headers);
		return forkreq;
	}
    
    
    public static void sendLinkedRequest(SipServletRequest req) throws ServletException, IOException {
    	B2buaHelper b2b = req.getB2buaHelper();
		SipSession linkedSession = b2b.getLinkedSession(req.getSession());
		if (linkedSession != null) {
			SipServletRequest linkedReq = b2b.createRequest(linkedSession, req, null);
			copyContent(req, linkedReq);
			linkedReq.send();
			logger.debug("[out] {}", dumpSipRequest(linkedReq));
		}	
		else 
			throw new ServletException("linkedSession is not found for request "+ dumpSipRequest(req));
    }
	
    
    /**
     * Finds out the all uncommitted messages for a SipSession, which can be extracted from request parameter  
     */
    public static List<SipServletMessage> getUncommittedMsgs(SipServletRequest req, UAMode mode) {
    	List<SipServletMessage> result = new ArrayList<SipServletMessage>();
    	SipSession initSession = req.getSession();
		B2buaHelper b2b = req.getB2buaHelper();
		result.addAll(b2b.getPendingMessages(initSession, mode));
		
		SipSession linkedSession = b2b.getLinkedSession(req.getSession());
		if (linkedSession != null)
			result.addAll(b2b.getPendingMessages(linkedSession, mode));
		return result;
    }
    
    
    /**
     * Gets uncommitted requests from given request and if there are a BYEs among them, sends Bye to the upstream
     * and sends OK on initial Bye 
     */
    public static void closeDialog(SipServletRequest initReq) {
//    	dumpAllUncommited(initReq);
		for (SipServletMessage msg : getUncommittedMsgs(initReq, UAMode.UAS)) {
			try {
				SipServletRequest req = (SipServletRequest) msg;
				if (req.getMethod().equals("BYE")) {
					SipUtils.sendLinkedRequest(req); // send bye to the other side
					SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
//					copyContent(req, resp);
					RLC rlc = IsupUtils.addSipiRLC(resp);
					resp.send(); 					 //send OK on first Bye
					logger.debug("[out] {} RLC {}", dumpSipResponse(resp), rlc.toString());
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
    
    
    public static void sendByeToBothSides(SipServletRequest initReq) {
    	try {
    		SipSession initSession = initReq.getSession();
    		SipServletRequest byeForInitial = initSession.createRequest("BYE");
			byeForInitial.send();
			logger.debug("[out] {}", dumpSipRequest(byeForInitial));
			
			B2buaHelper b2b = initReq.getB2buaHelper();
			SipSession linkedSession = b2b.getLinkedSession(initSession);
			SipServletRequest byeForLinked =  linkedSession.createRequest("BYE");
			byeForLinked.send();
			logger.debug("[out] {}", dumpSipRequest(byeForLinked));
			
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
    	
    }
    
    
	public static void dumpAllUncommited(SipServletRequest req) {
		SipSession initSession = req.getSession();
		B2buaHelper b2b = req.getB2buaHelper();
		
		
		for (SipServletMessage msg : b2b.getPendingMessages(initSession, UAMode.UAC)) {
			logger.debug(UAMode.UAC + " Message: " + msg.toString());
		}
		
		for (SipServletMessage msg : b2b.getPendingMessages(initSession, UAMode.UAS)) {
			logger.debug(UAMode.UAS + " Message: " + msg.toString());
		}
		
		SipSession linkedSession = b2b.getLinkedSession(req.getSession());
		if (linkedSession == null) {
			logger.debug("Linked SipSession does not exist! ");
			return;
		}	
		for (SipServletMessage msg : b2b.getPendingMessages(linkedSession, UAMode.UAC)) {
			logger.debug(UAMode.UAC + " Message in LinkedSession: " + msg.toString());
		}
		
		for (SipServletMessage msg : b2b.getPendingMessages(linkedSession, UAMode.UAS)) {
			logger.debug(UAMode.UAS + " Message in LinkedSession: " + msg.toString());
		}
	}
    
	
    public static void copyContent(SipServletMessage source, SipServletMessage dest) throws IOException {
        if (source.getContentLength() > 0) {
            if (source.getContent() instanceof String) {
                dest.setContent(((String) source.getContent()).getBytes(), source.getContentType());
            } else {
                dest.setContent(source.getContent(), source.getContentType());
            }

            String enc = source.getCharacterEncoding();
            if (enc != null && enc.length() > 0) {
                dest.setCharacterEncoding(enc);
            }
        }
    }

    
	
    public static String dumpSipRequest(SipServletRequest req) {
        return String.format("req method:%s, %s, %s", 
        		req.getMethod(), req.getRequestURI(), dumpSipMsg(req));
    }

    public static String dumpSipResponse(SipServletResponse resp) {
        return String.format("resp status:%s, %s", 
        		resp.getStatus(), dumpSipMsg(resp));
    }

    public static String dumpSipMsg(SipServletMessage sipMsg) {
        return String.format("From:%s, To:%s, Call-ID:%s, SAS:%s", 
        		sipMsg.getFrom().getURI(), 
        		sipMsg.getTo().getURI(), 
        		sipMsg.getCallId(), 
        		sipMsg.getApplicationSession().getId());
    }
}
