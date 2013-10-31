package org.icearchiva.lta.service.model;

import java.util.List;

import org.icearchiva.lta.ws.v1.ArchiveRequest;

public interface IAIPArchiveRequest {
	
	public String getAipId();
	public IUser getUser();
	public ArchiveRequest getArchiveRequest();
	public List<IFileInfo> getFileInfoList();
	public String getContentsPath();

}
