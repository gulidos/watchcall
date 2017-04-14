package ru.in.watcher.sip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.servlet.sip.*;

//@SipListener
public class LifecycleListener implements SipSessionListener, SipSessionAttributeListener, 
        SipApplicationSessionListener, SipApplicationSessionAttributeListener {

	 private static final Logger logger = LoggerFactory.getLogger(LifecycleListener.class);

    @Override
    public void sessionCreated(SipSessionEvent ev) {
        if (logger.isDebugEnabled()) {
            logger.debug("SES " +
            		ev.getSession().getId() + " : " +
            		ev.getSession().getLocalParty() + "->" +
                    ev.getSession().getRemoteParty() + "/ " +
                    ev.getSession().getCallId() +
                    " APP=" + ev.getSession().getApplicationSession().getId());
        }
    }

    @Override
    public void sessionDestroyed(SipSessionEvent ev) {
        if (logger.isDebugEnabled()) {
            logger.debug("SES " +
            		ev.getSession().getId() + " : " +
            		ev.getSession().getCallId() +
                    " APP=" + ev.getSession().getApplicationSession().getId());
        }
    }

    @Override
    public void sessionReadyToInvalidate(SipSessionEvent ev) {
        if (logger.isDebugEnabled()) {
            logger.debug("SES " +
            		ev.getSession().getId() + " : " +
            		ev.getSession().getCallId() +
                    " APP=" + ev.getSession().getApplicationSession().getId());
        }
    }

    @Override
    public void attributeAdded(SipSessionBindingEvent ev) {
    	if (logger.isDebugEnabled()) {
            logger.debug("BindAttr name: " +
            		ev.getName() + " class: " +
            		ev.getClass() + " object: " +
            		ev.getSource() + 
                    " APP=" + ev.getSession().getApplicationSession().getId());
        }
    }


    @Override
    public void attributeRemoved(SipSessionBindingEvent ev) {
    	if (logger.isDebugEnabled()) {
            logger.debug("BindAttr name: " +
            		ev.getName() + " class: " +
            		ev.getClass() + " object: " +
            		ev.getSource() + 
                    " APP=" + ev.getSession().getApplicationSession().getId());
        }
    }


    @Override
    public void attributeReplaced(SipSessionBindingEvent ev) {
    	if (logger.isDebugEnabled()) {
            logger.debug("BindAttr name: " +
            		ev.getName() + " class: " +
            		ev.getClass() + " object: " +
            		ev.getSource() + 
                    " APP=" + ev.getSession().getApplicationSession().getId());
        }
    }

    @Override
    public void sessionCreated(SipApplicationSessionEvent ev) {
        if (logger.isDebugEnabled()) {
            logger.debug("APP " + ev.getApplicationSession().getId());
        }
    }

    @Override
    public void sessionDestroyed(SipApplicationSessionEvent ev) {
        if (logger.isDebugEnabled()) {
            logger.debug("APP " + ev.getApplicationSession().getId());
        }
    }

    @Override
    public void sessionExpired(SipApplicationSessionEvent ev) {
        if (logger.isDebugEnabled()) {
            logger.debug("APP " + ev.getApplicationSession().getId());
        }
    }

    @Override
    public void sessionReadyToInvalidate(SipApplicationSessionEvent ev) {
        if (logger.isDebugEnabled()) {
            logger.debug("APP " + ev.getApplicationSession().getId());
        }
    }

	@Override
	public void attributeAdded(SipApplicationSessionBindingEvent ev) {
		if (logger.isDebugEnabled()) {
            logger.debug("SAS BindAttr name: " +
            		ev.getName() + " class: " +
            		ev.getClass() + 
                    " APP=" + ev.getApplicationSession().getId());
		}    
	}

	@Override
	public void attributeRemoved(SipApplicationSessionBindingEvent ev) {
		if (logger.isDebugEnabled()) {
            logger.debug("SAS BindAttr name: " +
            		ev.getName() + " class: " +
            		ev.getClass() + 
                    " APP=" + ev.getApplicationSession().getId());
		}  
		
	}

	@Override
	public void attributeReplaced(SipApplicationSessionBindingEvent ev) {
		if (logger.isDebugEnabled()) {
            logger.debug("SAS BindAttr name: " +
            		ev.getName() + " class: " +
            		ev.getClass() + 
                    " APP=" + ev.getApplicationSession().getId());
		}  
		
	}
}
