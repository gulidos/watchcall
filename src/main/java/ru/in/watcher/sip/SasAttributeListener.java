package ru.in.watcher.sip;

import javax.servlet.sip.SipApplicationSessionAttributeListener;
import javax.servlet.sip.SipApplicationSessionBindingEvent;
import javax.servlet.sip.annotation.SipListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SipListener
public class SasAttributeListener implements SipApplicationSessionAttributeListener {
	private static final Logger logger = LoggerFactory.getLogger(SasAttributeListener.class);

	public SasAttributeListener() {
		logger.info("SasAttributeListener created");
	}


	@Override
	public void attributeAdded(SipApplicationSessionBindingEvent ev) {
		
	}	
	
	@Override
	public void attributeRemoved(SipApplicationSessionBindingEvent ev) {
		
	}

	@Override
	public void attributeReplaced(SipApplicationSessionBindingEvent ev) {
		
	}

}
