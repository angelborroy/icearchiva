package org.icearchiva.lta.services.aip;

import org.icearchiva.lta.service.model.IAIPFile;

public class MetsFile implements IAIPFile {
	
	private String fileName;
	private String mimeType;
	private Long size;
	private String checksum;
	private String checksumType;
	private String locType;
	private String href;

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public String getMimeType() {
		return mimeType;
	}

	@Override
	public Long getSize() {
		return size;
	}

	@Override
	public String getChecksum() {
		return checksum;
	}

	@Override
	public String getChecksumType() {
		return checksumType;
	}

	@Override
	public String getLocType() {
		return locType;
	}

	@Override
	public String getHRef() {
		return href;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public void setChecksumType(String checksumType) {
		this.checksumType = checksumType;
	}

	public void setLocType(String locType) {
		this.locType = locType;
	}

}
