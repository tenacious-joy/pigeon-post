package com.pigeon.post.messaging.messagingdemo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pigeon.post.messaging.messagingdemo.model.MessageRequest;
import com.pigeon.post.messaging.messagingdemo.model.MessageResponse;
import com.pigeon.post.messaging.messagingdemo.model.PhoneNumber;
import com.pigeon.post.messaging.messagingdemo.services.MessagingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
@AutoConfigureMockMvc
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MessagingService messagingServiceMock;
    
    @Autowired
    private RedisTemplate redisTemplate;

    @Before
    public void setUp() {
        messagingServiceMock = mock(MessagingService.class);
    }

    @Test
    public void testReceiveMessageOkResponse() throws Exception{
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setFrom("4924195509197");
        messageRequest.setTo("4924195509197");
        messageRequest.setText("qwerty");

        when(messagingServiceMock.fetchContactNumber(messageRequest.getFrom())).thenReturn(messageRequest.getFrom());
        when(messagingServiceMock.fetchContactNumber(messageRequest.getTo())).thenReturn(messageRequest.getTo());

        MvcResult result = mockMvc.perform(post("/message/receive")
                .header("Authorization", "Basic cGxpdm8xOjIwUzBLUE5PSU0=")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(messageRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        MessageResponse response = asObject(result.getResponse().getContentAsString());
        assertThat(response.getMessage(), is("inbound sms ok"));
    }

    @Test
    public void testReceiveMessageSTOPScenario() throws Exception{
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setFrom("4924195509197");
        messageRequest.setTo("4924195509197");
        messageRequest.setText("STOP");

        when(messagingServiceMock.fetchContactNumber(messageRequest.getFrom())).thenReturn(messageRequest.getFrom());
        when(messagingServiceMock.fetchContactNumber(messageRequest.getTo())).thenReturn(messageRequest.getTo());

        mockMvc.perform(post("/message/receive")
                .header("Authorization", "Basic cGxpdm8xOjIwUzBLUE5PSU0=")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(messageRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        PhoneNumber phoneNumber = (PhoneNumber) redisTemplate.opsForValue().get(messageRequest.getFrom()+messageRequest.getTo());
        assertThat(phoneNumber.getText(), is("STOP"));

        mockMvc.perform(post("/message/send")
                .header("Authorization", "Basic cGxpdm8xOjIwUzBLUE5PSU0=")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(messageRequest)))
                .andExpect(status().isForbidden())
                .andDo(print())
                .andReturn();
    }

    @Test
    public void testRequestLimitSendMessage() throws Exception{
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setFrom("4924195509049");
        messageRequest.setTo("4924195509197");
        messageRequest.setText("STOP");

        when(messagingServiceMock.fetchContactNumber(messageRequest.getFrom())).thenReturn(messageRequest.getFrom());
        when(messagingServiceMock.fetchContactNumber(messageRequest.getTo())).thenReturn(messageRequest.getTo());

        for(int i=0;i<50;i++) {
            mockMvc.perform(post("/message/send")
                    .header("Authorization", "Basic cGxpdm8xOjIwUzBLUE5PSU0=")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(messageRequest)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn();
        }

        mockMvc.perform(post("/message/send")
                .header("Authorization", "Basic cGxpdm8xOjIwUzBLUE5PSU0=")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(messageRequest)))
                .andExpect(status().isBandwidthLimitExceeded())
                .andDo(print())
                .andReturn();
    }

    @Test
    public void testReceiveMessageBadRequest() throws Exception{
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setFrom("1234");
        messageRequest.setTo("0987654321");
        messageRequest.setText("qwerty");

        when(messagingServiceMock.fetchContactNumber(messageRequest.getFrom())).thenReturn(messageRequest.getFrom());
        when(messagingServiceMock.fetchContactNumber(messageRequest.getTo())).thenReturn(messageRequest.getTo());

        MvcResult result = mockMvc.perform(post("/message/receive")
                .header("Authorization", "Basic cGxpdm8xOjIwUzBLUE5PSU0=")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(messageRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();
        MessageResponse response = asObject(result.getResponse().getContentAsString());
        assertThat(response.getError(), is("from recipient's number should be between 6 and 16"));
    }

    @Test
    public void testReceiveMessageBadRequestInvalidData() throws Exception{
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setFrom("4924195509197");
        messageRequest.setTo("0987qwqrwq");
        messageRequest.setText("qwerty");

        when(messagingServiceMock.fetchContactNumber(messageRequest.getFrom())).thenReturn(messageRequest.getFrom());
        when(messagingServiceMock.fetchContactNumber(messageRequest.getTo())).thenReturn(messageRequest.getTo());

        MvcResult result = mockMvc.perform(post("/message/receive")
                .header("Authorization", "Basic cGxpdm8xOjIwUzBLUE5PSU0=")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(messageRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();
        MessageResponse response = asObject(result.getResponse().getContentAsString());
        assertThat(response.getError(), is("to is invalid"));
    }

    @Test
    public void testReceiveMessageMethodNotAllowed() throws Exception{
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setFrom("1234");
        messageRequest.setTo("0987654321");
        messageRequest.setText("qwerty");

        when(messagingServiceMock.fetchContactNumber(messageRequest.getFrom())).thenReturn(messageRequest.getFrom());
        when(messagingServiceMock.fetchContactNumber(messageRequest.getTo())).thenReturn(messageRequest.getTo());

        mockMvc.perform(delete("/message/receive")
                .header("Authorization", "Basic cGxpdm8xOjIwUzBLUE5PSU0=")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(messageRequest)))
                .andExpect(status().isMethodNotAllowed())
                .andDo(print());
    }

    @Test
    public void testBadUserAuthentication() throws Exception{
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setFrom("4924195509197");
        messageRequest.setTo("4924195509197");
        messageRequest.setText("qwerty");

        when(messagingServiceMock.fetchContactNumber(messageRequest.getFrom())).thenReturn(messageRequest.getFrom());
        when(messagingServiceMock.fetchContactNumber(messageRequest.getTo())).thenReturn(messageRequest.getTo());

        mockMvc.perform(post("/message/receive")
                .header("Authorization", "Basic cGxpdm8xMToyMFMwS1BOT0lN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(messageRequest)))
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andReturn();
    }

    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final String jsonContent = mapper.writeValueAsString(obj);
            return jsonContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static MessageResponse asObject(final String val) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final MessageResponse response = mapper.readValue(val, MessageResponse.class);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
