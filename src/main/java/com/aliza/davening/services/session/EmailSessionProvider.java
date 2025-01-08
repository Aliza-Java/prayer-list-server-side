package com.aliza.davening.services.session;

import jakarta.mail.Session;

public interface EmailSessionProvider {
    Session getSession();
}