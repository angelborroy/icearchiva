package org.icearchiva.lta.service.model;

public interface IAIPFile {
	
	public String getFileName();
	public String getMimeType();
	public Long getSize();
	public String getChecksum();
	public String getChecksumType();
	public String getLocType();
	public String getHRef();

}
