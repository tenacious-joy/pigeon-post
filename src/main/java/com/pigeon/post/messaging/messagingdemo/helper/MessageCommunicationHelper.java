package com.pigeon.post.messaging.messagingdemo.helper;

import com.pigeon.post.messaging.messagingdemo.model.MessageRequest;
import com.pigeon.post.messaging.messagingdemo.model.MessageResponse;
import com.pigeon.post.messaging.messagingdemo.model.PhoneNumber;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.pigeon.post.messaging.messagingdemo.helper.Literals.FROM;
import static com.pigeon.post.messaging.messagingdemo.helper.Literals.STOP;

@Service
public class MessageCommunicationHelper {

    public boolean sendOrBlock(MessageRequest messageRequest, MessageResponse messageResponse, RedisTemplate<String, Object> redisTemplate) {
        if(redisTemplate.hasKey(messageRequest.getFrom())) {
            PhoneNumber phoneNumber = (PhoneNumber) redisTemplate.opsForValue().get(messageRequest.getFrom());
            if (isSendingLimitExceeded(messageResponse, phoneNumber)) return true;
            phoneNumber.setText(messageRequest.getText());
            phoneNumber.setCount(phoneNumber.getCount()+1);
            redisTemplate.opsForValue().set(messageRequest.getFrom(), phoneNumber);
        } else {
            cacheRequest(messageRequest, 24, FROM, redisTemplate);
        }
        return false;
    }

    public boolean isUserRegisteredForDND(MessageRequest messageRequest, MessageResponse messageResponse, PhoneNumber value) {
        if (null != value && STOP.equalsIgnoreCase(value.getText())) {
            messageResponse.setError(String.join(" ", "sms from",
                    messageRequest.getFrom(), "to",
                    messageRequest.getTo(),
                    "blocked by STOP request"));
            return true;
        }
        return false;
    }

    public void cacheRequest(MessageRequest messageRequest, int i, String cacheType, RedisTemplate<String, Object> redisTemplate) {
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setFrom(messageRequest.getFrom());
        phoneNumber.setTo(messageRequest.getTo());
        phoneNumber.setNow(Date.from(Instant.now()));
        phoneNumber.setCount(1);
        phoneNumber.setText(messageRequest.getText());
        String key = STOP.equalsIgnoreCase(cacheType) ? messageRequest.getFrom()+messageRequest.getTo()
                : messageRequest.getFrom();
        redisTemplate.opsForValue().set(key, phoneNumber);
        redisTemplate.expire(key, i, TimeUnit.HOURS);
    }

    private boolean isSendingLimitExceeded(MessageResponse messageResponse, PhoneNumber phoneNumber) {
        if(phoneNumber.getCount() == 50) {
            messageResponse.setError("“limit reached for from "+phoneNumber.getFrom());
            return true;
        }
        return false;
    }
}
