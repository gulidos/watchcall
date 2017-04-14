package ru.in.watcher.service;

import org.apache.commons.configuration2.Configuration;
import org.springframework.context.ApplicationEvent;

public class ReloadEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;
	private final String eventName;
	
	public ReloadEvent(Configuration source, String eventName) {
		super(source);
		this.eventName = eventName;
	}

	public String getEventName() {return eventName;}

	@Override
	public String toString() {
		return "ReloadEvent [eventName=" + eventName + "]";
	}

	
}
