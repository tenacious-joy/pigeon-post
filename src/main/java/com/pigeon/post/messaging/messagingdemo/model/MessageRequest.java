package com.pigeon.post.messaging.messagingdemo.model;

import com.pigeon.post.messaging.messagingdemo.utils.isValidNumber;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class MessageRequest {

    @NotNull(message = "from is missing")
    @Size(min = 6, max = 16, message = "from recipient's number should be between 6 and 16")
    @isValidNumber(message = "from is invalid")
    private String from;

    @NotNull(message = "to is missing")
    @Size(min = 6, max = 16, message = "to recipient's number should be between 6 and 16")
    @isValidNumber(message = "to is invalid")
    private String to;

    @NotEmpty(message = "text is missing")
    @Size(max = 120)
    private String text;
}
