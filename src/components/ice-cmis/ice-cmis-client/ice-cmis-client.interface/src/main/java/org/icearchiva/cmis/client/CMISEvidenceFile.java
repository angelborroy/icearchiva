package org.icearchiva.cmis.client;


public interface CMISEvidenceFile extends CMISFile {
	
	// Extracted from evidence-doc-type.xml custom cmis type
	public static final String PROPERTY_DOC_ID = "evidenceDocumentType";
	public static final String PROPERTY_TRANSACTION_ID = "transactionId";
	public static final String PROPERTY_TRANSACTION_STATUS = "transactionStatus";

	// Transaction status allowed values
	public static final String TRANSACTION_STATUS_GRANTED = "GRANTED";
	public static final String TRANSACTION_STATUS_PENDING = "PENDING";
	public static final String TRANSACTION_STATUS_REJECTED = "REJECTED";
	
	public String getTransactionId();
	public String getTransactionStatus();

}
