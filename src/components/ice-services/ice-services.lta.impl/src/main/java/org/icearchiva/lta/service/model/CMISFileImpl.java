package org.icearchiva.lta.service.model;

import java.io.InputStream;

import org.icearchiva.cmis.client.CMISFile;

public class CMISFileImpl implements CMISFile {
	
	private InputStream inputStream;
	private String fileName;
	private String mimeType;
	
	@Override
	public InputStream getInputStream() {
		return inputStream;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public String getMimeType() {
		return mimeType;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

}
