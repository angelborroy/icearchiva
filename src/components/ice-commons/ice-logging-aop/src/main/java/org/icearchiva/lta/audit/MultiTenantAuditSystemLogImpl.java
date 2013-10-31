package org.icearchiva.lta.audit;

import org.icearchiva.commons.session.ICurrentSessionIdentifierResolver;
import org.icearchiva.commons.tenancy.logger.IMultiTenancyLoggerResolver;


public class MultiTenantAuditSystemLogImpl implements IAuditSystem {
	
    private IMultiTenancyLoggerResolver multiTenancyLoggerResolver;
    private ICurrentSessionIdentifierResolver sessionResolver;

	public IMultiTenancyLoggerResolver getMultiTenancyLoggerResolver() {
		return multiTenancyLoggerResolver;
	}

	public void setMultiTenancyLoggerResolver(IMultiTenancyLoggerResolver multiTenancyLoggerResolver) {
		this.multiTenancyLoggerResolver = multiTenancyLoggerResolver;
	}

	public ICurrentSessionIdentifierResolver getSessionResolver() {
		return sessionResolver;
	}

	public void setSessionResolver(ICurrentSessionIdentifierResolver sessionResolver) {
		this.sessionResolver = sessionResolver;
	}

	@Override
	public void logRequest(String message) {
		multiTenancyLoggerResolver.resolveCurrentTenantLogger().info(
				sessionResolver.resolveCurrentSessionIdentifier() + ":\n" + message);
	}

	@Override
	public void logResponse(String message) {
		multiTenancyLoggerResolver.resolveCurrentTenantLogger().info(
				sessionResolver.resolveCurrentSessionIdentifier() + ":\n" + message);
	}

}
