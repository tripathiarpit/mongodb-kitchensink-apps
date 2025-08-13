package com.mongodb.kitchensink.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
