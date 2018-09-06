package com.pigeon.post.messaging.messagingdemo.controllers;

import com.pigeon.post.messaging.messagingdemo.model.MessageRequest;
import com.pigeon.post.messaging.messagingdemo.model.MessageResponse;
import com.pigeon.post.messaging.messagingdemo.model.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.Instant;
import java.util.Date;

@RestController
public class MessageController {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    // @CachePut(value = "messageResponse", key = "#messageRequest.from")
    @RequestMapping(value = "/receiveMessage", method = RequestMethod.POST)
    public MessageResponse receiveMessage(@Valid @RequestBody MessageRequest messageRequest) {
        if ("STOP".equals(messageRequest.getText())) {

            if (redisTemplate.hasKey(messageRequest.getFrom())) {
                PhoneNumber phoneNumber = (PhoneNumber) redisTemplate.opsForValue().get(messageRequest.getFrom());
                phoneNumber.setCount(phoneNumber.getCount()+1);
                redisTemplate.opsForValue().set(messageRequest.getFrom(), phoneNumber);
            } else {
                PhoneNumber phoneNumber = new PhoneNumber();
                phoneNumber.setFrom(messageRequest.getFrom());
                phoneNumber.setTo(messageRequest.getTo());
                phoneNumber.setNow(Date.from(Instant.now()));
                phoneNumber.setCount(1);
                redisTemplate.opsForValue().set(messageRequest.getFrom(), phoneNumber);
            }
        }
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage("inbound sms ok");
        return messageResponse;
    }
}
