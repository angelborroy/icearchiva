package org.icearchiva.lta.service.model;

import java.util.Date;

public class AIPPackageImpl implements IAIPPackage {
	
	private String tenantId;
	private String referenceId;
	private String transactionId;
	private Date transactionDate;
	private String status;
	private Date stampingDate;
	
	@Override
	public String getTenantId() {
		return tenantId;
	}

	@Override
	public String getReferenceId() {
		return referenceId;
	}

	@Override
	public String getTransactionId() {
		return transactionId;
	}

	@Override
	public String getStatus() {
		return status;
	}
	
	@Override
	public Date getTransactionDate() {
		return transactionDate;
	}
	
	@Override
	public Date getStampingDate() {
		return stampingDate;
	}
	
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}
	
	public void setStampingDate(Date stampingDate) {
		this.stampingDate = stampingDate;
	}

}
