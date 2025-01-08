package com.aliza.davening.services.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import jakarta.mail.Session;

@Service
@Profile("test")
public class TestEmailSessionProvider implements EmailSessionProvider {

    private final Session testSession;

    @Autowired
    public TestEmailSessionProvider(Session testSession) {
        this.testSession = testSession;
    }

    @Override
    public Session getSession() {
        return testSession;
    }
}