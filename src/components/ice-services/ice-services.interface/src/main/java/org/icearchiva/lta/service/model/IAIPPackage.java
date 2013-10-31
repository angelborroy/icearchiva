package org.icearchiva.lta.service.model;

import java.util.Date;

public interface IAIPPackage {
	
	public String getTenantId();
	public String getReferenceId();
	public String getTransactionId();
	public Date getTransactionDate();
	public String getStatus();
	public Date getStampingDate();

}
