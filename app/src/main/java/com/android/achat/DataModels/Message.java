package com.android.achat.DataModels;

import java.text.SimpleDateFormat;

/**
 * Created by Gaurav on 15-09-2017.
 */

public class Message {
    private String messageId;
    private String message;
    private long time;
    private String type;
    private String seen;
    private String from;

    public Message(){

    }

    public Message(long time, String type, String seen, String message, String from) {
        this.time = time;
        this.type = type;
        this.seen = seen;
        this.message = message;
        this.from = from;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        String strTime = sdf.format(this.time);
        return strTime;
    }

    public long getLongTime(){
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String isSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }
}
