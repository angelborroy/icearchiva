package org.icearchiva.lta.service;

import java.io.InputStream;

import org.icearchiva.lta.service.model.IESignResponse;

public interface ESignService {
	
	public IESignResponse getXAdES_A(InputStream xmlFile);
	
	public IESignResponse addTimeStamp(InputStream xmlFileSigned);
	
	public boolean verifyXAdES_A(InputStream xmlFileSigned);
	
	public InputStream extractXMLInfoFromSignature(InputStream xmlFileSigned);
	
	public IESignResponse checkAsyncTransaction(String transactionId);

	public void setAipService(AIPService aipService);

	public AIPService getAipService();
	
	public void setTsaService(TsaService tsaService);
	
	public TsaService getTsaService();
	
}
