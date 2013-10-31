package org.icearchiva.lta.service.model;

import java.io.InputStream;
import java.util.Date;

public interface IESignResponse {
	
	public String getTransactionId();
	public String getTransactionStatus();
	public InputStream getSignedContent();
	public Date getStampingDate();

}
