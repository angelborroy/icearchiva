package org.icearchiva.cmis.client;

import java.util.List;

public interface CMISClient {
	
	public static final String PURGABLE_FOLDER_PREFIX = "purge-";

	public String getContentsPath();
	
	public Integer getRepoFolderNestingLevels();
	
	public void uploadContentToPurgeableFolder(CMISFile cmisFile);

	public void uploadDescriptorToPurgeableFolder(CMISEvidenceFile cmisFile);
	
	public void updateDescriptorFile(String tenandId, String uuid, CMISEvidenceFile cmisFile);

	public void renamePurgeableUuidFolder();
	
	public void updateMimeTypeOnPurgeable(CMISFile cmisFile, String mimeType);

	public void removeResource(String uuid);
	
	public CMISEvidenceFile getDescriptorFile(String uuid, String descriptorFileName);
	
	public List<CMISFile> getContentFiles(String uuid);
	
	public List<String> getListIds();
	
	// Asynchronous operations: tenantId required
	
	public CMISEvidenceFile getDescriptorFile(String tenantId, String uuid, String descriptorFileName);
	
	public void removePurgeableResources(String tenantId, String uuid);

}
