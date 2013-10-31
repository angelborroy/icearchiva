package org.icearchiva.lta.service.model;

public interface IFileInfo {
	
    public String getName();
    public String getMimeType();
	public String getHash();
	public String getHashType();
    public Long getSize();

}
