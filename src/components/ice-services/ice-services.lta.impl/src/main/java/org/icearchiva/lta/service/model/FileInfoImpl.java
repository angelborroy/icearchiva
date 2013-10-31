package org.icearchiva.lta.service.model;

import org.icearchiva.lta.service.model.IFileInfo;

public class FileInfoImpl implements IFileInfo {
	
	private String name;
	private String mimeType;
	private String hash;
	private String hashType;
	private Long size;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getHashType() {
		return hashType;
	}
	public void setHashType(String hashType) {
		this.hashType = hashType;
	}
	public Long getSize() {
		return size;
	}
	public void setSize(Long size) {
		this.size = size;
	}
}
