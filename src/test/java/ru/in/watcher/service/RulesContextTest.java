package ru.in.watcher.service;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import ru.in.watcher.conf.RootConfig;
import ru.in.watcher.service.Rules;
import ru.in.watcher.web.WebConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RootConfig.class, WebConfig.class })
@WebAppConfiguration
public class RulesContextTest {
	@Autowired Rules rules;

	@Test
	public void test() throws Exception {
		Assert.assertEquals(rules.applyB("74957757575"), "89258762694");
	}
	
}
