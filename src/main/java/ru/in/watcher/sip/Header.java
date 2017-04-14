package ru.in.watcher.sip;

import java.util.Map;

import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipURI;

public class Header {
	private final SipURI sipuri;
	private final Map<String, String> params;
	
	
	public Header(SipURI uri, Map<String, String> map) throws ServletParseException {
		this.sipuri = uri;
		this.params = map;
	}

	public SipURI getSipuri() {	return sipuri;}
	public Map<String, String> getParams() {return params;	}

}
