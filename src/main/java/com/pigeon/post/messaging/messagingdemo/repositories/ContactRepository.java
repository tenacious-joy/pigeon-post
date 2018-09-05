package com.pigeon.post.messaging.messagingdemo.repositories;

import com.pigeon.post.messaging.messagingdemo.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer> {

    Contact findContactByNumber(String number);
}
