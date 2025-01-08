package com.aliza.davening.services.session;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.mail.Session;

@Configuration
@Profile("test")
public class TestEmailSessionConfig {

    @Bean
    public Session testMailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.host", "localhost");
        props.put("mail.smtp.port", "25"); 
        return Session.getInstance(props);
    }
}