package org.icearchiva.lta.service.model;

import java.util.List;


public interface IDescriptorDetails {
	
	public String getPolicyId();
	public String getReferenceId();
	public List<IAIPFile> getFiles();
	public IAIPMetadata getMetadata(String fileName);

}
