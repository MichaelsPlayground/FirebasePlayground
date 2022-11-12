package de.androidcrypto.firebaseplayground.models;

import java.util.HashMap;
import java.util.Map;

public class MessageModel {

    String message;
    long messageTime;
    boolean messageEncrypted;

    public MessageModel() {

    }

    public MessageModel(String message, long messageTime, boolean messageEncrypted) {
        this.message = message;
        this.messageTime = messageTime;
        this.messageEncrypted = messageEncrypted;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("messageTime", messageTime);
        result.put("messageEncrypted", messageEncrypted);
        return result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public boolean isMessageEncrypted() {
        return messageEncrypted;
    }

    public void setMessageEncrypted(boolean messageEncrypted) {
        this.messageEncrypted = messageEncrypted;
    }
}
