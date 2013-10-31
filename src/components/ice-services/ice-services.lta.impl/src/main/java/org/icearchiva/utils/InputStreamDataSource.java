package org.icearchiva.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

public class InputStreamDataSource implements DataSource {
	
	private InputStream is;
	private String name;
	private String contentType;
	
	public InputStreamDataSource(InputStream is, String name, String contentType) {
		this.is = is;
		this.name = name;
		this.contentType = contentType;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return is;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		throw new UnsupportedOperationException("Not implemented");
	}
	
}

