package ru.in.watcher.conf;

import java.io.File;

import javax.annotation.PostConstruct;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

import ru.in.watcher.service.ReloadEvent;


@Component
public class Config implements ApplicationEventPublisherAware {
	private static final Logger logger = LoggerFactory.getLogger(Config.class);

	private volatile Configuration cfg; 
	private final File propertiesFile;
	private ApplicationEventPublisher applicationEventPublisher = null;

	
	public Config(String fileName) {
		logger.debug("Instantiate ...");
		propertiesFile = new File(fileName);
	}
	
	public Config() {
		logger.debug("Instantiate ...");
		String home = System.getProperty("user.home");
		propertiesFile = new File(home + "/rules.cfg");
	}
	
	@PostConstruct
	public void load() throws ConfigurationException {
		Configurations configs = new Configurations();
		cfg = configs.properties(propertiesFile);
		applicationEventPublisher.publishEvent(new ReloadEvent(cfg, "reload"));
	}
	

	public Configuration get() {return cfg;}


	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher arg0) {
		this.applicationEventPublisher = arg0;

	}

	
	public static void main(String[] args) throws ConfigurationException, InterruptedException {
		Config c = new Config();
		c.load();
		String h = c.cfg.getString("database.host");
		System.out.println(h);

		for (Object s : c.cfg.getList("rule"))
			System.out.println((String) s);

		Thread.sleep(10000);
		c.load();
		System.out.println(c.cfg.getString("database.host"));
	}
}
