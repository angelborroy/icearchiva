package org.icearchiva.lta.audit;


public interface IAuditSystem {
	
	public void logRequest(String message);
	public void logResponse(String message);

}
