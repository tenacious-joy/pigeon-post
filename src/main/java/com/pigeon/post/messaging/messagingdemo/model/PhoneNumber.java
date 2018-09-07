package com.pigeon.post.messaging.messagingdemo.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class PhoneNumber implements Serializable {
    private String from;
    private String to;
    private int count;
    private Date now;
    private String text;
}
