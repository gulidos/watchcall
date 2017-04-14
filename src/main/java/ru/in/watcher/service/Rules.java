package ru.in.watcher.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
@Component
public class Rules  implements ApplicationListener<ReloadEvent> {
	private static final Logger logger = LoggerFactory.getLogger(Rules.class);

	private volatile List<String[]> rulesA;
	private volatile List<String[]> rulesB;
	
	public Rules() {
		logger.debug("Instantiating ...");
	}
	
	private List<String[]> load(List<String> list) {
		if (list == null)
			return null;
		List<String[]> newRules = new ArrayList<String[]>();
		
		for (String s: list) {
			String [] a = s.split("\\s+");
			if	(a.length == 2) {
				a = Arrays.copyOf(a, 3);  // if the rule hasn't prefix that has to be added
				a[2] = "";
			}	
				
			if (isOk(a))
				newRules.add(a);
		}
		return newRules;
	}
	
	
	public void loadA(List<String> list) {
		rulesA = load(list);
		logger.debug("riles for A number: {}", rulesA.size());
	}
	
	public void loadB(List<String> list) {
		rulesB = load(list);
		logger.debug("riles for B number: {}", rulesB.size());
	}
	
	
	private boolean isOk(String[] a) {
		if (a.length != 3)
			return false;
		for (String s: a) {
			for (char ch: s.toCharArray())
				if (!Character.isDigit(ch) && ch != '+') {
					System.err.println(s + " is not verified");
					return false;
				}	
		}
		return true;
	}
	
	
	private String[] find(String number, List<String[]> rules) {
		if (number == null || number.length() == 0)
			return null;
		String[] res = null;
		int longest = 0;
		for (String[] a: rules) {
			if (number.startsWith(a[0]) && a[0].length() > longest) {
				res = a;
				longest = a[0].length();
			}	
		}
		return res;
	}
	
	
	private String apply(String n, List<String[]> rules) {
		if (n == null || n.length() == 0) return null;
		String[] rule = find(n, rules);
		if (rule == null ) 
			return n;	
		
		int cut = Integer.parseInt(rule[1]);
		if (cut == 0 )
			return n;
		
		return (rule[2] + n.substring(cut));	
	}
	
	
	public String applyA(String n) {
		return apply(n, rulesA);
	}
	
	public String applyB(String n) {
		
		return apply(n, rulesB);
	}
	
	
	@Override
	public void onApplicationEvent(ReloadEvent ev) {
		logger.debug("applicationEventPublisher ReloadEvent recieved");
		Configuration cfg = (Configuration) ev.getSource();
		
		List<Object> la = cfg.getList("A");
		if (la != null) {
			List<String> lst = new ArrayList<String>();
			for (Object s : cfg.getList("A"))
				lst.add((String) s);
			loadA(lst);
		}
		
		List<Object> lb = cfg.getList("B");
		if (lb != null) {
			List<String> lst = new ArrayList<String>();
			for (Object s: cfg.getList("B"))
				lst.add((String) s);
			loadB(lst);
		}
	}
}
