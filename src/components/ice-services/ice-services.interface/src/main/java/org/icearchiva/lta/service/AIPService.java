package org.icearchiva.lta.service;

import java.io.InputStream;

import org.icearchiva.lta.service.model.IAIPArchiveRequest;
import org.icearchiva.lta.service.model.IDescriptorDetails;


public interface AIPService {
	
	public String getPackageId();

	public boolean isValidationEnabled();
	
	public String getFileName();
	
	public String getMimeType();
	
	public String getXMLRootElementId();
	
	public InputStream getPackage(IAIPArchiveRequest aipArchiveRequest);
	
	public IDescriptorDetails getDetails(InputStream is);

}
