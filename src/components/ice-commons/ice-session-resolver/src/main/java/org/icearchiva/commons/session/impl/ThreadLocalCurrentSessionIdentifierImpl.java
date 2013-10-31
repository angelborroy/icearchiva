package org.icearchiva.commons.session.impl;

import org.icearchiva.commons.session.ICurrentSessionIdentifierResolver;
import org.icearchiva.commons.session.ICurrentSessionIdentifierSetter;

public class ThreadLocalCurrentSessionIdentifierImpl implements ICurrentSessionIdentifierResolver, ICurrentSessionIdentifierSetter{

    private static ThreadLocal<String> currentSessionId = new ThreadLocal<String>();
	
	@Override
	public void setCurrentSessionId(String sessionId) {
        currentSessionId.set(sessionId);
	}

	@Override
	public String resolveCurrentSessionIdentifier() {
        return currentSessionId.get();
	}

}
