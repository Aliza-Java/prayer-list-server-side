package com.aliza.davening.services.session;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;

@Configuration
@Profile("!test")
public class ProdEmailSessionConfig {
	
	@Value("${spring.mail.username}")
	String emailUsername;
	@Value("${spring.mail.password}")
	String emailPassword;
	@Value("${spring.mail.port}")
	int emailPort;
	@Value("${spring.mail.host}")
	String emailHost;

	@Bean
    public Session prodMailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", emailHost);
        props.put("mail.smtp.port", emailPort);

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUsername, emailPassword);
            }
        });
    }
}
