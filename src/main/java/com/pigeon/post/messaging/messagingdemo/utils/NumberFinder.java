package com.pigeon.post.messaging.messagingdemo.utils;

import com.pigeon.post.messaging.messagingdemo.services.MessagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Service
public class NumberFinder implements ConstraintValidator<isFound, String> {

    @Autowired
    MessagingService messagingService;
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return messagingService.fetchContactNumber(value) != null;
    }
}
