package ru.in.watcher.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@ComponentScan({"ru.in.watcher.conf", "ru.in.watcher.service", "ru.in.watcher.web"})
@Configuration
public class RootConfig {
	private static final Logger logger = LoggerFactory.getLogger(RootConfig.class);
	
	@Bean
	public Settings settings()  { 
		logger.debug("Instantiate Settings...");
		return new Settings();
	}

}
