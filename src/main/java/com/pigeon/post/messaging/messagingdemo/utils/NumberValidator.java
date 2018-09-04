package com.pigeon.post.messaging.messagingdemo.utils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NumberValidator implements ConstraintValidator<isValidNumber, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            Long.parseLong(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
