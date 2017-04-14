package ru.in.watcher.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ru.in.watcher.service.Calls;

public class Settings implements ApplicationContextAware {

	public static final String OS_USER_NAME = System.getProperty("user.name");
	private static final String SETTINGS_FILE_NAME = System.getProperty("user.home") + "/core.properties";

	private static final Logger logger = LoggerFactory.getLogger(Settings.class);
	private static ApplicationContext context;

	
	// SIP
	public volatile static int SIP_IS_REJECT_NEW_INVITES = 0;
	

	public volatile static String SIP_OUTGOING_IP_ARRD = "10.33.90.129";
	public volatile static int SIP_OUTGOING_PORT = 6060;
	

	public volatile static int SIP_REJECT_CODE = 410;
	public volatile static String SIP_REJECT_MSG = "Reject";

	public volatile static int DEFAULT_NO_ANSW_TIMER = 40000;
	
	   // isup

    public volatile static int FEATURE_ISUP_ENABLED = 0;

    public volatile static int ISUP_CGCAT = 10;

    public volatile static int ISUP_PREANS_BCI0_MASK = 252;

    public volatile static int ISUP_PREANS_BCI0_VALUE = 20;

    public volatile static int ISUP_PREANS_BCI1_MASK = 143;

    public volatile static int ISUP_PREANS_BCI1_VALUE = 4;

    public volatile static int ISUP_PREANS_EVENT = 1;


	@Autowired  Calls calls;

	public Settings() {
		loadSettings();
	}

	public void loadSettings() {
//		
	}

	

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		Settings.context = context;
	}

	public static ApplicationContext getContext() {
		return context;
	}


}
