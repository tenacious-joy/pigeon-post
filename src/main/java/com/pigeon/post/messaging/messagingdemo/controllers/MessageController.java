package com.pigeon.post.messaging.messagingdemo.controllers;

import com.pigeon.post.messaging.messagingdemo.model.MessageRequest;
import com.pigeon.post.messaging.messagingdemo.model.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Slf4j
public class MessageController {

    @RequestMapping(value = "/receiveMessage", method = RequestMethod.POST)
    public ResponseEntity receiveMessage(@Valid @RequestBody MessageRequest messageRequest) {
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage("inbound sms ok");
        return ResponseEntity.ok(messageResponse);
    }
}
