package com.pigeon.post.messaging.messagingdemo.services;

import com.pigeon.post.messaging.messagingdemo.model.Contact;
import com.pigeon.post.messaging.messagingdemo.repositories.ContactRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessagingService {

    @Autowired
    private ContactRepository contactRepository;

    public String fetchContactNumber(String number) {
        try {
            Contact contact = contactRepository.findContactByNumber(number);
            return contact != null ? contact.getNumber() : null;
        } catch (Exception e) {
           // log.error("Unable to fetch phone number from the repository", e);
        }
        return null;
    }
}
