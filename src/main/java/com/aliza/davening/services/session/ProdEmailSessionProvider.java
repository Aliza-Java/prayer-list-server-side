package com.aliza.davening.services.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import jakarta.mail.Session;

@Service
@Profile("prod")
public class ProdEmailSessionProvider implements EmailSessionProvider {

	private final Session prodSession;

	@Autowired
	public ProdEmailSessionProvider(Session prodSession) {
		this.prodSession = prodSession;
	}

	@Override
	public Session getSession() {
		return prodSession;
	}

}