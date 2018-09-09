package com.pigeon.post.messaging.messagingdemo.controllers;

import com.pigeon.post.messaging.messagingdemo.configurations.Complete;
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
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @GetMapping
    public ResponseEntity<String> ping() {
        return new ResponseEntity<String>("pong", HttpStatus.OK);
    }

    @RequestMapping(value = "/receive", method = RequestMethod.POST)
    public ResponseEntity<MessageResponse> receiveMessage(@Validated(Complete.class) @RequestBody MessageRequest messageRequest) {
        if ("STOP".equals(messageRequest.getText())) {
            cacheRequest(messageRequest, 4, "stop");
        }
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage("inbound sms ok");
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody MessageRequest messageRequest) {
        MessageResponse messageResponse = new MessageResponse();
        PhoneNumber value = (PhoneNumber) redisTemplate.opsForValue().get(messageRequest.getFrom()+messageRequest.getTo());
        if (null != value && "STOP".equalsIgnoreCase(value.getText())) {
            messageResponse.setError(String.join(" ", "sms from",
                    messageRequest.getFrom(), "to",
                    messageRequest.getTo(),
                    "blocked by STOP request"));
            return new ResponseEntity<>(messageResponse, HttpStatus.FORBIDDEN);
        }
        if(redisTemplate.hasKey(messageRequest.getFrom())) {
            PhoneNumber phoneNumber = (PhoneNumber) redisTemplate.opsForValue().get(messageRequest.getFrom());
            if(phoneNumber.getCount() == 50) {
                messageResponse.setError("“limit reached for from "+phoneNumber.getFrom());
                return new ResponseEntity<>(messageResponse, HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
            }

            phoneNumber.setCount(phoneNumber.getCount()+1);
            redisTemplate.opsForValue().set(messageRequest.getFrom(), phoneNumber);
        } else {
            cacheRequest(messageRequest, 24, "from");
        }

        messageResponse.setMessage("outbound sms ok");
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }

    private void cacheRequest(MessageRequest messageRequest, int i, String cacheType) {
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setFrom(messageRequest.getFrom());
        phoneNumber.setTo(messageRequest.getTo());
        phoneNumber.setNow(Date.from(Instant.now()));
        phoneNumber.setCount(1);
        phoneNumber.setText(messageRequest.getText());
        String key = "stop".equalsIgnoreCase(cacheType) ? messageRequest.getFrom()+messageRequest.getTo()
                : messageRequest.getFrom();
        redisTemplate.opsForValue().set(key, phoneNumber);
        redisTemplate.expire(key, i, TimeUnit.HOURS);
    }
}
