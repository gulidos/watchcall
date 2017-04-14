package ru.in.watcher.sip;

import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.annotation.SipListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.in.watcher.service.Call;

@SipListener
public class TimerLst implements TimerListener {
	private static final Logger logger = LoggerFactory.getLogger(TimerLst.class);

	public TimerLst() {
	}

	@Override
	public void timeout(ServletTimer timer) {
		try {
//			String timerName = timer.getInfo().toString();

			SipApplicationSession sas = timer.getApplicationSession();
			Call call = (Call) sas.getAttribute(Call.ATTR_CALL);

			logger.debug("Timer {} fired. Id: {} call: {}", timer.getInfo(), timer.getId(), call.toString());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	

}
