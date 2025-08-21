package com.mongodb.kitchensink.service;

import jakarta.activation.DataHandler;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailServiceImpl Tests")
class EmailServiceImplTest {

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "username", "testuser@example.com");
        ReflectionTestUtils.setField(emailService, "password", "testpassword");
        ReflectionTestUtils.setField(emailService, "host", "smtp.test.com");
        ReflectionTestUtils.setField(emailService, "port", 587);
    }

    @Test
    @DisplayName("sendEmail should prepare and send a correct message")
    void sendEmail_shouldPrepareAndSendCorrectMessage() throws Exception {
        try (MockedStatic<Transport> mockedTransport = org.mockito.Mockito.mockStatic(Transport.class)) {
            // Given
            String to = "recipient@example.com";
            String subject = "Test Subject";
            String body = "<h1>Hello!</h1><p>This is a test email.</p>";
            ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

            // When
            emailService.sendEmail(to, subject, body);

            // Then
            mockedTransport.verify(() -> Transport.send(messageCaptor.capture()));

            Message capturedMessage = messageCaptor.getValue();
            assertEquals(subject, capturedMessage.getSubject());
            assertEquals(to, capturedMessage.getAllRecipients()[0].toString());

            // The correct way to verify content type and content
            DataHandler dataHandler = capturedMessage.getDataHandler();
            assertEquals("text/html", dataHandler.getContentType().split(";")[0]);
            assertEquals(body, dataHandler.getContent());
        }
    }

    @Test
    @DisplayName("sendEmail should throw RuntimeException on MessagingException")
    void sendEmail_shouldThrowRuntimeExceptionOnMessagingException() {
        try (MockedStatic<Transport> mockedTransport = org.mockito.Mockito.mockStatic(Transport.class)) {
            // Given
            String to = "recipient@example.com";
            String subject = "Test Subject";
            String body = "Test Body";

            mockedTransport.when(() -> Transport.send(any(Message.class))).thenThrow(new MessagingException("Connection failed"));

            // When/Then
            RuntimeException thrown = assertThrows(RuntimeException.class, () -> emailService.sendEmail(to, subject, body));

            assertEquals("Failed to send email", thrown.getMessage());
            assertEquals(MessagingException.class, thrown.getCause().getClass());
        }
    }
}