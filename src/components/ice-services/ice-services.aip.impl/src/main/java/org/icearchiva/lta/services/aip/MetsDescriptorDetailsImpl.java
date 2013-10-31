package org.icearchiva.lta.services.aip;

import java.util.List;
import java.util.Map;

import org.icearchiva.lta.service.model.IAIPFile;
import org.icearchiva.lta.service.model.IAIPMetadata;
import org.icearchiva.lta.service.model.IDescriptorDetails;

public class MetsDescriptorDetailsImpl implements IDescriptorDetails {
	
	private String referenceId;
	private String policyId;
	private Map<String, IAIPMetadata> metadataMap;
	private List<IAIPFile> files;
	
	@Override
	public String getPolicyId() {
		return policyId;
	}
	
	@Override
	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public void setPolicyId(String policyId) {
		this.policyId = policyId;
	}

	@Override
	public IAIPMetadata getMetadata(String fileName) {
		return metadataMap.get(fileName);
	}
	
	public void setMetadataMap(Map<String, IAIPMetadata> metadataMap) {
		this.metadataMap = metadataMap;
	}

	@Override
	public List<IAIPFile> getFiles() {
		return files;
	}
	
	public void setFiles(List<IAIPFile> files) {
		this.files = files;
	}

}
