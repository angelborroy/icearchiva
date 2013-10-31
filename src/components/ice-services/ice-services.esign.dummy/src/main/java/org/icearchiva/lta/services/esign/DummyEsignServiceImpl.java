package org.icearchiva.lta.services.esign;

import java.io.InputStream;

import org.icearchiva.cmis.client.CMISEvidenceFile;
import org.icearchiva.lta.service.AIPService;
import org.icearchiva.lta.service.ESignService;
import org.icearchiva.lta.service.TsaService;
import org.icearchiva.lta.service.model.IESignResponse;
import org.icearchiva.lta.services.esign.model.DummyESignResponseImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyEsignServiceImpl implements ESignService {
	private static final Logger logger = LoggerFactory.getLogger(DummyEsignServiceImpl.class);
	
	AIPService aipService;
	TsaService tsaService;

	@Override
	public IESignResponse getXAdES_A(InputStream xmlFile) {
		DummyESignResponseImpl response = new DummyESignResponseImpl();
		response.setTransactionStatus(CMISEvidenceFile.TRANSACTION_STATUS_GRANTED);		
		return response;
	}

	@Override
	public IESignResponse addTimeStamp(InputStream xmlFileSigned) {
		DummyESignResponseImpl response = new DummyESignResponseImpl();
		response.setTransactionStatus(CMISEvidenceFile.TRANSACTION_STATUS_GRANTED);		
		return response;
	}

	@Override
	public boolean verifyXAdES_A(InputStream xmlFileSigned) {		
		return true;
	}

	@Override
	public InputStream extractXMLInfoFromSignature(InputStream xmlFileSigned) {	
		return xmlFileSigned;
	}

	@Override
	public IESignResponse checkAsyncTransaction(String transactionId) {
		DummyESignResponseImpl response = new DummyESignResponseImpl();
		response.setTransactionStatus(CMISEvidenceFile.TRANSACTION_STATUS_GRANTED);		
		return response;
	}

	@Override
	public void setAipService(AIPService aipService) {
		this.aipService = aipService;
	}

	@Override
	public AIPService getAipService() {
		return aipService;
	}

	@Override
	public TsaService getTsaService() {
		return tsaService;
	}

	@Override
	public void setTsaService(TsaService tsaService) {
		this.tsaService = tsaService;
	}
	
}
