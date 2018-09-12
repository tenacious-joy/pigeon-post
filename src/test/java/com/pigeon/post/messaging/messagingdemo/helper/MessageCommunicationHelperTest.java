package com.pigeon.post.messaging.messagingdemo.helper;

import com.pigeon.post.messaging.messagingdemo.model.MessageRequest;
import com.pigeon.post.messaging.messagingdemo.model.MessageResponse;
import com.pigeon.post.messaging.messagingdemo.model.PhoneNumber;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageCommunicationHelperTest {

    private MessageCommunicationHelper messageCommunicationHelper;
    private RedisTemplate redisTemplateMock;
    private MessageRequest messageRequest;
    private MessageResponse messageResponse;
    private PhoneNumber phoneNumber;

    @Before
    public void setUp() {
        messageCommunicationHelper = new MessageCommunicationHelper();
        messageRequest = new MessageRequest();
        messageResponse = new MessageResponse();
        phoneNumber = new PhoneNumber();

        redisTemplateMock=  mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOperations =mock(ValueOperations.class);
        when(redisTemplateMock.opsForValue()).thenReturn(valueOperations);

        messageRequest.setFrom("1234");
        messageRequest.setTo("5678");
        messageRequest.setText("test");
    }

    @Test
    public void testSendOrBlockCacheScenario() {

        redisTemplateMock.opsForValue().set("1234", phoneNumber);
        boolean result = messageCommunicationHelper.blockMessageCommunication(messageRequest, messageResponse, redisTemplateMock);
        assertFalse(result);
    }

    @Test
    public void testisUserRegisteredForDND() {

        messageRequest.setText("hello");

        phoneNumber.setFrom("1234");
        phoneNumber.setTo("5678");
        phoneNumber.setText("stop");
        phoneNumber.setNow(new Date());
        phoneNumber.setCount(1);

        redisTemplateMock.opsForValue().set("1234", phoneNumber);
        boolean result = messageCommunicationHelper.isUserRegisteredForDND(
                messageRequest, messageResponse, phoneNumber);
        assertTrue(result);
    }
}
