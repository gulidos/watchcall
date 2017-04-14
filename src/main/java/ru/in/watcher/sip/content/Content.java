package ru.in.watcher.sip.content;

public class Content {
    public byte[] raw;
    public String type;

    public Content(byte[] raw, String type) {
        this.raw = raw;
        this.type = type;
    }
    
    
}