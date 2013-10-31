package org.icearchiva.cmis.client.model;

import org.icearchiva.cmis.client.CMISEvidenceFile;

public class CMISEvidenceFileImpl extends CMISFileImpl implements CMISEvidenceFile {
	
	private String transactionId;
	private String transactionStatus;

	@Override
	public String getTransactionId() {
		return transactionId;
	}

	@Override
	public String getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

}
