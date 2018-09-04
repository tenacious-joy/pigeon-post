package com.pigeon.post.messaging.messagingdemo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pigeon.post.messaging.messagingdemo.model.MessageRequest;
import com.pigeon.post.messaging.messagingdemo.model.MessageResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testReceiveMessageOkResponse() throws Exception{
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setFrom("1234567890");
        messageRequest.setTo("0987654321");
        messageRequest.setText("qwerty");

        MvcResult result = mockMvc.perform(post("/receiveMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(messageRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        MessageResponse response = asObject(result.getResponse().getContentAsString());
        assertThat(response.getMessage(), is("inbound sms ok"));
    }

    @Test
    public void testReceiveMessageBadRequest() throws Exception{
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setFrom("1234");
        messageRequest.setTo("0987654321");
        messageRequest.setText("qwerty");

        MvcResult result = mockMvc.perform(post("/receiveMessage")
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
        messageRequest.setFrom("1234567890");
        messageRequest.setTo("0987qwqrwq");
        messageRequest.setText("qwerty");

        MvcResult result = mockMvc.perform(post("/receiveMessage")
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

        mockMvc.perform(delete("/receiveMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(messageRequest)))
                .andExpect(status().isMethodNotAllowed())
                .andDo(print());
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
