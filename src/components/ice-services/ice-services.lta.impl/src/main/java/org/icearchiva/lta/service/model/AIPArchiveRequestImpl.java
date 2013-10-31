package org.icearchiva.lta.service.model;

import java.util.List;

import org.icearchiva.lta.service.model.IAIPArchiveRequest;
import org.icearchiva.lta.service.model.IFileInfo;
import org.icearchiva.lta.service.model.IUser;
import org.icearchiva.lta.ws.v1.ArchiveRequest;

public class AIPArchiveRequestImpl implements IAIPArchiveRequest {
	
	String aipId;
	IUser user;
	ArchiveRequest archiveRequest;
	List<IFileInfo> fileInfoList;
	String contentsPath;
	
	@Override
	public String getAipId() {
		return aipId;
	}
	public void setAipId(String aipId) {
		this.aipId = aipId;
	}
	@Override
	public IUser getUser() {
		return user;
	}
	public void setUser(IUser user) {
		this.user = user;
	}
	@Override
	public ArchiveRequest getArchiveRequest() {
		return archiveRequest;
	}
	public void setArchiveRequest(ArchiveRequest archiveRequest) {
		this.archiveRequest = archiveRequest;
	}
	@Override
	public List<IFileInfo> getFileInfoList() {
		return fileInfoList;
	}
	public void setFileInfoList(List<IFileInfo> fileInfoList) {
		this.fileInfoList = fileInfoList;
	}
	@Override
	public String getContentsPath() {
		return contentsPath;
	}
	public void setContentsPath(String contentsPath) {
		this.contentsPath = contentsPath;
	}
}
