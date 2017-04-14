package ru.in.watcher.service;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ru.in.watcher.service.Rules;



public class RulesTest {
	Rules r;
	@Before
	public void beforeTest() {
		r = new Rules();
		List<String> list = Arrays.asList(new String[] {
				"7		 0	   	0",
				"7495775		 4	   	",
				"+75		 3	   	85",
				"7495 1	  8  "
			});
			
			r.loadA(list);
			r.loadB(list);
	}

	@Test
	public void testLongest() {
		Assert.assertEquals(r.applyA("74957751111"), "7751111");
		Assert.assertEquals(r.applyA("79091234567"), "79091234567");
		Assert.assertEquals(r.applyA("+75091234567"), "85091234567");
		Assert.assertEquals(r.applyA("74951111122"), "84951111122");
	}

	
}
