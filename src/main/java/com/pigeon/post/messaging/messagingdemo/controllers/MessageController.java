package com.pigeon.post.messaging.messagingdemo.controllers;

import com.pigeon.post.messaging.messagingdemo.configurations.Complete;
import com.pigeon.post.messaging.messagingdemo.helper.MessageCommunicationHelper;
import com.pigeon.post.messaging.messagingdemo.model.MessageRequest;
import com.pigeon.post.messaging.messagingdemo.model.MessageResponse;
import com.pigeon.post.messaging.messagingdemo.model.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.pigeon.post.messaging.messagingdemo.helper.Literals.*;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    MessageCommunicationHelper messageCommunicationHelper;


    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @GetMapping
    public ResponseEntity<String> ping() {
        return new ResponseEntity<String>("pong", HttpStatus.OK);
    }

    @RequestMapping(value = "/receive", method = RequestMethod.POST)
    public ResponseEntity<MessageResponse> receiveMessage(@Validated(Complete.class) @RequestBody MessageRequest messageRequest) {
        if (STOP.equalsIgnoreCase(messageRequest.getText())) {
            messageCommunicationHelper.cacheRequest(messageRequest, 4, STOP, redisTemplate);
        }
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage(INBOUND_SUCCESS);
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public ResponseEntity<MessageResponse> sendMessage(@Validated(Complete.class) @RequestBody MessageRequest messageRequest) {
        MessageResponse messageResponse = new MessageResponse();
        PhoneNumber value = (PhoneNumber) redisTemplate.opsForValue().get(messageRequest.getFrom()+messageRequest.getTo());
        if (messageCommunicationHelper.isUserRegisteredForDND(messageRequest, messageResponse, value))
            return new ResponseEntity<>(messageResponse, HttpStatus.FORBIDDEN);
        if (messageCommunicationHelper.blockMessageCommunication(messageRequest, messageResponse, redisTemplate))
            return new ResponseEntity<>(messageResponse, HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
        messageResponse.setMessage(OUTBOUND_SUCCESS);
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }
}
