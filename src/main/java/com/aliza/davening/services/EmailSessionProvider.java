package com.aliza.davening.services;

import jakarta.mail.Session;

public interface EmailSessionProvider {
    Session getSession();
}