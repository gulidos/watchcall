package ru.in.watcher.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class Calls {
	private static final Logger logger = LoggerFactory.getLogger(Calls.class);
	private final ConcurrentMap<String, Call> dialogs;
	
	@Autowired private Rules rules;
	
	public Calls() {	
		logger.debug("Instantiate the Calls service...");
		dialogs  = new ConcurrentHashMap<String, Call>();
	}
	
	
	public void addCallId(Call call) {
		if (dialogs.putIfAbsent(call.getSas().getId(), call) != null)
			throw new IllegalStateException("Call Id " + call.getSas().getId() + " already exists in dialogs");
	}
	
	
	public void removeCallId(Call call) {
		String callId = call.getSas().getId();
		if (callId == null || dialogs.remove(callId) == null)
			logger.error("unknown dialogId: " + callId );
		logger.debug("clear call data");
	}
	
	
	public int size() {
		return dialogs.size();
	}
	
	
	public Call createNew(SipServletRequest incomeRequest) throws ServletParseException {
		Call call = new Call(incomeRequest);
		String newDst = rules.applyB(call.getDst());
		call.setDst(newDst);
		addCallId(call);
		return call;
	}
}
