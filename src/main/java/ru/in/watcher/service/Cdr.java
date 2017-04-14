package ru.in.watcher.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cdr {
	private static final Logger logger = LoggerFactory.getLogger(Cdr.class);
	private final Call call;
	private final Date initTime;
	private Date startTime;
	private Date stopTime;
	private final SimpleDateFormat sdf;
	private int cause;
	
	public Cdr(Call call) {
		this.call = call;
		this.initTime = new Date();
		sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		
	}
	
	public void stop(int cause) {
		setStopTime(new Date());
		this.cause = cause;
		write();
	}

	public void start( ) {
		setStartTime(new Date());
	}
	
	public int getDuration() {
		if (startTime == null) 	
			return 0;
		else if (stopTime == null) 
			return (int) (new Date().getTime() - startTime.getTime())/1000;
		else
			return (int) (stopTime.getTime() - startTime.getTime())/1000;
	}
	
	public String toCdr() {
		return String.format("%s, %s, %s, %d, %s, %s, %d", 
				formatDate(initTime),
				formatDate(startTime),
				formatDate(stopTime),
				getDuration(),
				call.getSrc(),
				call.getDst(),
				cause);
	}
	
	public void write() {
		logger.info(toCdr());
	}
	
	
	public String formatDate(Date date) {
		return date!= null ? sdf.format(date) : "";
	}

	public Call getCall() {	return call;	}

	public Date getStartTime() {return startTime;}
	private void setStartTime(Date startTime) {	this.startTime = startTime;	}
	
	public Date getStopTime() {	return stopTime;	}
	private void setStopTime(Date stopTime) {this.stopTime = stopTime;	}
	
	public Date getInitTime() {	return initTime;}
}
