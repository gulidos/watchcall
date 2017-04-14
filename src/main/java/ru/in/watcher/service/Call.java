package ru.in.watcher.service;

import java.io.Serializable;

import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;

import ru.in.watcher.sip.Header;
import ru.in.watcher.sip.Headers;
import ru.in.watcher.sip.SipUtils;

public class Call implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String ATTR_CALL = "ATTR_CALL";	
	private String src;
	private String dst;
	private final Cdr cdr;
	private boolean isCallActive = false;
	private final SipServletRequest incomeRequest;
	private SipServletRequest legBInvite;
	private final SipApplicationSession sas;
	private int causeFrom18x = 0;

	
	Call(SipServletRequest incomeRequest) throws ServletParseException  {
		this.incomeRequest = incomeRequest;
		this.sas = incomeRequest.getApplicationSession();
		sas.setAttribute(ATTR_CALL, this);
	
		Header hdr = SipUtils.getHeader(incomeRequest, Headers.FROM);
		this.src = hdr.getSipuri().getUser();

		hdr = SipUtils.getHeader(incomeRequest, Headers.TO);
		this.dst = hdr.getSipuri().getUser();
		
		this.cdr = new Cdr(this);
	}

	
	public String getSrc() {	return src;}

	public void setSrc(String src) {	this.src = src;}

	public String getDst() {	return dst;}

	public void setDst(String dst) {	this.dst = dst;}

	public void setCallActive(boolean b) {isCallActive = b;}
	
	public boolean isCallActive() { 	return isCallActive;	}

	public SipServletRequest getIncomeRequest() {return incomeRequest;	}

	public SipApplicationSession getSas() {	return sas;}
	public Cdr getCdr() {return cdr;}
	
	public SipServletRequest getLegBInvite() {return legBInvite;}
	public void setLegBInvite(SipServletRequest legBInvite) {this.legBInvite = legBInvite;}

	public int getCauseFrom18x() {return causeFrom18x;}
	public void setCauseFrom18x(int causeFrom18x) {this.causeFrom18x = causeFrom18x;}
	@Override
	public String toString() {
		return "Call srcAddress=" + src + ", dstAddress=" + dst
				 + ", callState=" + "]";
	}
}

