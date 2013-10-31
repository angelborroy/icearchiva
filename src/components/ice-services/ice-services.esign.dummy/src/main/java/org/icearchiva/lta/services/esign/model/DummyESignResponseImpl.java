package org.icearchiva.lta.services.esign.model;

import java.io.InputStream;
import java.util.Date;

import org.icearchiva.lta.service.model.IESignResponse;

public class DummyESignResponseImpl implements IESignResponse {
	
	private String transactionId;
	private String transactionStatus;
	private InputStream signedContent;
	private Date stampingDate;

	@Override
	public String getTransactionId() {
		return transactionId;
	}

	@Override
	public String getTransactionStatus() {
		return transactionStatus;
	}

	@Override
	public InputStream getSignedContent() {
		return signedContent;
	}
	
	@Override
	public Date getStampingDate() {
		return stampingDate;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	public void setSignedContent(InputStream signedContent) {
		this.signedContent = signedContent;
	}
	
	public void setStampingDate(Date stampingDate) {
		this.stampingDate = stampingDate;
	}

}
