package org.icearchiva.cmis.client;

import java.io.InputStream;

public interface CMISFile {
	
	public InputStream getInputStream();
	public String getFileName();
	public String getMimeType();
	
}
