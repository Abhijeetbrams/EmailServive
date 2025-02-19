package com.ecommerce.emailservice.consumers;

import com.ecommerce.emailservice.DTOs.SendEmailDTO;
import com.ecommerce.emailservice.utils.EmailUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

import javax.mail.Session;

@Service
public class SendEmailEventConsumer {
    ObjectMapper objectMapper;

    public SendEmailEventConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Step 5:- After Configuring the sendMessage in User Service here we will consume the message in Email Service
    @KafkaListener(topics = "SendEmail", groupId = "emailService")
    // We annotate the method with @KafkaListener and specify the topic name and group id
    // Why? - Because we want to listen to the topic SendEmail and whenever any message comes to the SendEmail topic
    // we want to consume the message as a part of the group group_id
    // Why we have to give the Group Id ? - Because Kafka works on the concept of Consumer Groups, and we have to specify the group id
    // and since if message comes in this topic and let suppose 10 instance of Email Service is running
    // then only one instance should consume the message.
    // Thus we have to specify the group id means Group Id is the logical grouping of the consumers.
    public void handleSendEmailEvent(String message) {
        // Step 6:- Consuming the message in the Email Service
        SendEmailDTO sendEmailEvent=null;
        try {
            sendEmailEvent = objectMapper.readValue(message, SendEmailDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // Step 8:- After Consuming the message we can write the logic to send the email to the user
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
        props.put("mail.smtp.port", "587"); //TLS Port
        props.put("mail.smtp.auth", "true"); //enable authentication
        props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS

        //create Authenticator object to pass in Session.getInstance argument
        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("randomtestuser167@gmail.com",
                        "ocqkllermmzdehck"); // app password
                // and this app password we can create by setting up the 2FA and
                // then google will create random the app password based on the Service name that we provide.
                // Why? - We don't want to expose the actual password in the code.
            }
        };
        //Get the Session object
        // Why ? - Because we need to send the email and for sending the email we need the session object
        // which is created using the properties and authenticator object
        Session session = Session.getInstance(props, auth);

        // Why ? - Because we need to send the email and for sending the email we need the session object.
        // which is created using the properties and authenticator object
        EmailUtil.sendEmail(session, sendEmailEvent.getRecipient(),sendEmailEvent.getSubject(), sendEmailEvent.getBody());

    }
}
