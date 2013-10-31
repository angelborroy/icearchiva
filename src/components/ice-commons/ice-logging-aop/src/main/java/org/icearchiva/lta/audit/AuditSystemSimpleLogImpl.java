package org.icearchiva.lta.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditSystemSimpleLogImpl implements IAuditSystem {
	
    private static final Logger log = LoggerFactory.getLogger(AuditSystemSimpleLogImpl.class);
    
	@Override
	public void logRequest(String message) {
		log.info(message);
	}

	@Override
	public void logResponse(String message) {
		log.info(message);
	}

}
