package ru.in.watcher.service;

import static ru.in.watcher.sip.SipUtils.dumpSipRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.UAMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.in.isp.IAM;
import ru.in.isp.IsupAddress;
import ru.in.watcher.conf.Settings;
import ru.in.watcher.sip.Headers;
import ru.in.watcher.sip.MainServlet;
import ru.in.watcher.sip.SipUtils;
import ru.in.watcher.sip.content.Content;
import ru.in.watcher.sip.content.Contents;
import ru.in.watcher.sip.content.IsupUtils;

public class CallRouting {

	private static final Logger logger = LoggerFactory.getLogger(CallRouting.class);

	
	public static void createAndSendInvite(Call call) {
		logger.debug("call: {}", call.toString());
		SipServletRequest incReq = call.getIncomeRequest();
		SipURI reqUri = (SipURI) incReq.getRequestURI();
		reqUri.setUser(call.getDst());
		reqUri.setHost(Settings.SIP_OUTGOING_IP_ARRD);
		reqUri.setPort(Settings.SIP_OUTGOING_PORT);
		
		
		SipURI from = (SipURI) incReq.getFrom().getURI().clone();
		from.setUser(call.getSrc());
		from.setHost("10.240.40.7");

		SipURI to = (SipURI) incReq.getTo().getURI().clone();
		to.setUser(call.getDst());
		to.setHost(Settings.SIP_OUTGOING_IP_ARRD);
		
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		headers.put(Headers.TO, Collections.singletonList(to.toString()));
		headers.put(Headers.FROM, Collections.singletonList(from.toString()));

		try {
			SipServletRequest newInvite = SipUtils.createB2bRequest(call.getIncomeRequest(), headers);
			newInvite.setRequestURI(reqUri);
			
			newInvite.getSession().setAttribute(MainServlet.ATTR_ORIG_REQ, incReq);
			
			byte[] sdpRaw = Contents.parseSdp(incReq).getBytes(Contents.CONTENT_ENCODING);
            Content sdp = new Content(sdpRaw, Contents.MIME_SDP);

			IAM iam = IsupUtils.createIAM(
                    new IsupAddress(call.getSrc(), IsupAddress.NAI_INT),
                    new IsupAddress(call.getDst(), IsupAddress.NAI_NAT)
            );
            Content multipart = Contents.addSipiToSdp(sdp, iam);	
            newInvite.setContent(multipart.raw, multipart.type);
            call.setLegBInvite(newInvite);
            
            newInvite.send();
			logger.debug("[out] {}", SipUtils.dumpSipRequest(newInvite));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			sendRejectOnInvite(call);
		}
	}

	
	
	private static void sendRejectOnInvite(Call call) {
		SipServletResponse resp = SipUtils.createRejectResponseOn(call.getIncomeRequest());
		sendResponse(resp);
	}

	
	public static void resendErrorResp(Call call, SipServletResponse resp ) {  
		try {	
			SipServletResponse other = SipUtils.createB2bResponse(resp);
			sendResponse(other);
		} catch (Exception  e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void sendUnavailableOnInvite(Call call, int code) {
		SipServletResponse resp = SipUtils.createUnavailableResponseOn(call.getIncomeRequest(), code);
		sendResponse(resp);
		logger.debug("[out] {}", SipUtils.dumpSipResponse(resp));
	}
	
	
	public static void sendFinalResponseOnInvite(Call call, int respCode) {
		SipServletRequest req = call.getIncomeRequest();
		SipServletResponse resp = req.createResponse(respCode);
		sendResponse(resp);
//		resp.getApplicationSession().invalidate();
	}

	

	public static void sendResponse(SipServletResponse resp) {
		try {
			resp.send();
			logger.debug("[out] {}", SipUtils.dumpSipResponse(resp));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	  /**
     * Sends Cancel to B part if answer timeout happened
     */
	public static void sendCancelOnBLeg(SipServletRequest initReq) {
		B2buaHelper b2b = initReq.getB2buaHelper();
		SipSession ss = b2b.getLinkedSession(initReq.getSession());
// !!! Do not work int OCCAS
		for (SipServletMessage msg : b2b.getPendingMessages(ss, UAMode.UAC)) {
			logger.debug(UAMode.UAC + " Message in LinkedSession: " + msg.toString());
			if (msg.getMethod().equals("INVITE")) {
				try {
					SipServletRequest cancel = ((SipServletRequest) msg).createCancel();
					cancel.send();
					logger.debug("[out] {}", dumpSipRequest(cancel));
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

		}
	}
}
