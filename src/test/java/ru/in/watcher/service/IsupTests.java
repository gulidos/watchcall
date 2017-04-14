package ru.in.watcher.service;

import org.junit.Test;

public class IsupTests {

	@Test
	public void test() {
		byte b = (byte) 0x94;
		byte mask = 0x7f;
		int i = (b & mask);
		System.out.println(i);
	}

}
