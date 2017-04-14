package ru.in.watcher.sip;

import static ru.in.watcher.sip.SipUtils.createB2bResponse;
import static ru.in.watcher.sip.SipUtils.createUnavailableResponseOn;
import static ru.in.watcher.sip.SipUtils.dumpSipRequest;
import static ru.in.watcher.sip.SipUtils.dumpSipResponse;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.UAMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.in.isp.Isup;
import ru.in.watcher.conf.Settings;
import ru.in.watcher.service.Call;
import ru.in.watcher.service.CallRouting;
import ru.in.watcher.service.Calls;
import ru.in.watcher.service.CauseCode;
import ru.in.watcher.service.Cdr;
import ru.in.watcher.sip.content.IsupUtils;

public class MainServlet extends javax.servlet.sip.SipServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(MainServlet.class);
	private String serverInfo;
	public static final String ATTR_ORIG_REQ = "ATTR_ORIG_REQ";
	private Calls calls;

	@Resource
	private transient SipFactory sipFactory;
	
	@Resource
	private transient TimerService ts;
	

	@Override
	public void init() throws ServletException {
		serverInfo = getServletContext().getServerInfo();
		SipUtils.setSipFactory(sipFactory);
		SipUtils.setTimerservice(ts);
		calls = (Calls) Settings.getContext().getBean("calls");
		logger.info("sip servlet initializing");
		super.init();
	}

	@Override
	public void destroy() {
		logger.info("destroying sip servlet ...");
		super.destroy();
	}
	
	
	@Override
	protected void doRegister(SipServletRequest req) throws ServletException, IOException {
		SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK); 
		resp.send();
		if(logger.isDebugEnabled()) 
			logger.debug("[out] {}", dumpSipResponse(resp));
	}

	
	@Override
	protected void doInvite(SipServletRequest req) throws ServletException, IOException {
		if (req.isInitial()) {
			if (Settings.SIP_IS_REJECT_NEW_INVITES == 1) {
				// 480 Temporarily unavailable
				SipServletResponse rejectResp = createUnavailableResponseOn(req, SipServletResponse.SC_REQUEST_TERMINATED);
				rejectResp.send();
				logger.debug("[out] {}", rejectResp.toString());
				return;
			}

			Call call = calls.createNew(req);
			CallRouting.createAndSendInvite(call);
			
		} else {
			logger.debug("Not Initial INVITE, REINVITE. Send 200/Ok.");
			SipServletResponse okResp = req.createResponse(SipServletResponse.SC_OK);
			okResp.send();
			logger.debug("[out] {}", okResp.toString());
			
		}
	}


	@Override
	protected void doBye(SipServletRequest req) throws ServletException, IOException {
		Call call = getCall(req);
//		CallLeg legId = 
//				call.getOrigSipSessionId().equals(req.getSession().getId()) 
//					? CallLeg.sendingSide : CallLeg.receivingSide; 
		
		Cdr cdr = call.getCdr();
		cdr.stop(17); 
		calls.removeCallId(call);
		SipUtils.closeDialog(call.getIncomeRequest());
	}

	
	@Override
	protected void doCancel(SipServletRequest req) throws ServletException, IOException {
		SipSession session = req.getSession();
		B2buaHelper helper = req.getB2buaHelper();
		SipSession linkedSession = helper.getLinkedSession(session);
		if (linkedSession != null) {
			SipServletRequest forkedReq = helper.createCancel(linkedSession);
			forkedReq.send();
			logger.debug("[out] {}", dumpSipRequest(forkedReq));
		} else {
			logger.error("linkedSession is not found for session with Call-ID:{}", session.getCallId());
		}
		
		Call call = getCall(req);
		call.getCdr().stop(127);
		calls.removeCallId(call);
	}

    @Override
	protected void doAck(SipServletRequest req) throws ServletException, IOException {
		B2buaHelper b2b = req.getB2buaHelper();
		SipSession ss = b2b.getLinkedSession(req.getSession());
		List<SipServletMessage> msgs = b2b.getPendingMessages(ss, UAMode.UAC);
		for (SipServletMessage msg : msgs) {
			SipServletResponse resp = (SipServletResponse) msg;
			if (resp.getStatus() == SipServletResponse.SC_OK) {		
				SipServletRequest ack = resp.createAck();
				SipUtils.copyContent(req, ack);
				ack.send();
				logger.debug("[out] {}", ack.toString());
			}
		}
    }


    
	@Override
	protected void doProvisionalResponse(SipServletResponse resp) throws ServletException, IOException {
		SipServletResponse otherResp = null;
		if (is18x(resp.getStatus())) {
			Isup isup = IsupUtils.getIsup(resp);
			if (isup != null) {
				logger.debug("[isup] {}", isup.toString());
				int cause = IsupUtils.getCause(isup);
				if (cause != 0) {
					sendCancel(resp, cause);
					return;
				}
				
				String rnn = IsupUtils.getRNN(isup);
				if (rnn != null) {
					sendCancel(resp, 20);
					return;
				}	
			}
		}
		otherResp = createB2bResponse(resp);
		otherResp.send();
		logger.debug("[out] {}", dumpSipResponse(otherResp));

	}

	private void sendCancel(SipServletResponse resp, int cause) throws IOException {
		logger.debug("code in 183 {}", cause);
		Call call = getCall(resp);
		call.setCauseFrom18x(cause);
		SipServletRequest cancel = call.getLegBInvite().createCancel();
		cancel.send();
		logger.debug("[out] {}", dumpSipRequest(cancel));
	}

	private boolean is18x(int status) {
		return (180 <= status && status < 190);
	}
	

	@Override
	protected void doSuccessResponse(SipServletResponse resp) throws ServletException, IOException {
		if (isResponseOnInvite(resp)) {
			doSuccessResponseOnInvite(resp);
		} else if (isResponseOnBye(resp)) {
			doSuccessResponseOnBye(resp);
		} else {
			logger.info("{}", resp);
		}
	}
	
	
	@Override
	protected void doErrorResponse(SipServletResponse resp) throws ServletException, java.io.IOException {
		logger.debug("resp code: {} reason: {}", resp.getStatus(), resp.getReasonPhrase());
		if (isResponseOnInvite(resp)) { 
			Call call = getCall(resp);
			if (call.getCauseFrom18x() != 0) {
				//TODO do mapping from cause code to SIP responses
			}
			
			switch (resp.getStatus()) {
			
			case 486: 
				CallRouting.resendErrorResp(call, resp);
				call.getCdr().stop(CauseCode.USER_BUSY.getCode());
				calls.removeCallId(call);
				break;
			case 408:
				// Timeout for an outgoing mesage
				CallRouting.resendErrorResp(call, resp);
				int cause = CauseCode.getCauseCodeFromSip(resp.getStatus());
				call.getCdr().stop(cause); 
				calls.removeCallId(call); 
				break;
			case 603: // busy here or the Decline button was pressed
				CallRouting.resendErrorResp(call, resp);
				call.getCdr().stop(CauseCode.USER_BUSY.getCode());
				calls.removeCallId(call);
			default:
				CallRouting.resendErrorResp(call, resp);
				cause = CauseCode.getCauseCodeFromSip(resp.getStatus());
				call.getCdr().stop(cause); 
				calls.removeCallId(call); 	
				break;
			}
		}
	}

	private boolean isResponseOnInvite(SipServletResponse resp) {
		return "INVITE".equals(resp.getMethod());
	}

	private boolean isResponseOnBye(SipServletResponse resp) {
		return "BYE".equals(resp.getMethod());
	}

	private void doSuccessResponseOnBye(SipServletResponse resp) throws IOException {
//		SipUtils.dumpAllUncommited(resp.getRequest());
		SipApplicationSession appSession = resp.getApplicationSession();
		appSession.invalidate();
		
	}

	private void doSuccessResponseOnInvite(SipServletResponse resp) throws IOException, ServletParseException {
		SipServletResponse otherResp = createB2bResponse(resp);
		otherResp.send();
		logger.debug("[out] {}", dumpSipResponse(otherResp));
		Call call = getCall(resp);
		call.setCallActive(true);
		call.getCdr().start();
	}
	

	private static Call getCall(SipServletMessage sipMsg) {
		return (Call) sipMsg.getApplicationSession().getAttribute(Call.ATTR_CALL);
	}

	
	@Override
	protected void doRequest(SipServletRequest req) throws ServletException, IOException {
		logger.debug("[in] {}", SipUtils.dumpSipRequest(req));
		try {
			super.doRequest(req);
		} catch (Exception e) {
			logger.error(e + ", Call-ID:" + req.getCallId(), e);
		}
	}

	
	@Override
	protected void doResponse(SipServletResponse resp) throws ServletException, IOException {
		logger.debug("[in] {}", SipUtils.dumpSipResponse(resp));
		try {
			super.doResponse(resp);
		} catch (ServletException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected void doOptions(SipServletRequest options) throws ServletException, IOException {
		SipServletResponse ok = options.createResponse(SipServletResponse.SC_OK);
		ok.addHeader("Server", serverInfo);
		// logger.info(" doOptions " + options.getInitialRemoteAddr());
		ok.send();
		if (logger.isDebugEnabled()) 
			logger.debug("[out] {}", dumpSipResponse(ok));
	}
}
