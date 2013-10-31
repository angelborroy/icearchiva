package org.icearchiva.cmis.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.icearchiva.cmis.client.model.CMISEvidenceFileImpl;
import org.icearchiva.cmis.client.model.CMISFileImpl;
import org.icearchiva.commons.session.ICurrentSessionIdentifierResolver;
import org.icearchiva.commons.tenancy.context.ICurrentTenantIdentifierResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AtomBindingCMISClientImpl implements CMISClient {
	
	private static final Logger log = LoggerFactory.getLogger(AtomBindingCMISClientImpl.class);
	
	private static final String CMIS_FOLDER = "cmis:folder";
	private static final String CMIS_DOCUMENT = "cmis:document";
	private static final String CONTENT_FOLDER_NAME = "contents";
	private static final int    NESTING_FOLDERS_NAME_SIZE = 2;
	private static final String CREATION_TOKEN = "Creation";
	
	private Integer repoFolderNestingLevels;
	private String user;
	private String password;
	private String repository;
	private String urlCmisServer;
	private ICurrentSessionIdentifierResolver sessionResolver;
	private ICurrentTenantIdentifierResolver multiTenantContextResolver;
	
	@Override
	public String getContentsPath() {
		return CONTENT_FOLDER_NAME;
	}

	public Integer getRepoFolderNestingLevels() {
		return repoFolderNestingLevels;
	}

	public void setRepoFolderNestingLevels(Integer repoFolderNestingLevels) {
		this.repoFolderNestingLevels = repoFolderNestingLevels;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getUrlCmisServer() {
		return urlCmisServer;
	}

	public void setUrlCmisServer(String urlCmisServer) {
		this.urlCmisServer = urlCmisServer;
	}

	public ICurrentSessionIdentifierResolver getSessionResolver() {
		return sessionResolver;
	}

	public void setSessionResolver(ICurrentSessionIdentifierResolver sessionResolver) {
		this.sessionResolver = sessionResolver;
	}

	public ICurrentTenantIdentifierResolver getMultiTenantContextResolver() {
		return multiTenantContextResolver;
	}

	public void setMultiTenantContextResolver(
			ICurrentTenantIdentifierResolver multiTenantContextResolver) {
		this.multiTenantContextResolver = multiTenantContextResolver;
	}
	
	@Override
	public void uploadContentToPurgeableFolder(CMISFile cmisFile) {
		Session session = createSession();
		Folder multitenantRootFolder = getMultitenantRootFolder(session, true);
		Folder uuidFolder = getPurgableUuidFolder(session, sessionResolver.resolveCurrentSessionIdentifier(), multitenantRootFolder, repoFolderNestingLevels, true);
		Folder contentsFolder = getFolderByPath(session, CONTENT_FOLDER_NAME, uuidFolder, true);
		Map<String, String> newDocProps = new HashMap<String, String>();
		newDocProps.put(PropertyIds.OBJECT_TYPE_ID, CMIS_DOCUMENT);
		newDocProps.put(PropertyIds.NAME, cmisFile.getFileName());
		ContentStream contentStream = new ContentStreamImpl(cmisFile.getFileName(), null, cmisFile.getMimeType(), cmisFile.getInputStream());
		contentsFolder.createDocument(newDocProps, contentStream, VersioningState.NONE, null, null, null, session.getDefaultContext());
	}
	
	@Override
	public void uploadDescriptorToPurgeableFolder(CMISEvidenceFile cmisFile) {
		Session session = createSession();
		Folder multitenantRootFolder = getMultitenantRootFolder(session, true);
		Folder uuidFolder = getPurgableUuidFolder(session, sessionResolver.resolveCurrentSessionIdentifier(), multitenantRootFolder, repoFolderNestingLevels, true);
		Map<String, String> newDocProps = new HashMap<String, String>();
		newDocProps.put(PropertyIds.OBJECT_TYPE_ID, CMISEvidenceFile.PROPERTY_DOC_ID);
		newDocProps.put(PropertyIds.NAME, cmisFile.getFileName());
		if (cmisFile.getTransactionId() != null && !cmisFile.getTransactionId().equals("")) {
			newDocProps.put(CMISEvidenceFile.PROPERTY_TRANSACTION_ID, cmisFile.getTransactionId());
		}
		newDocProps.put(CMISEvidenceFile.PROPERTY_TRANSACTION_STATUS, cmisFile.getTransactionStatus());
		ContentStream contentStream = new ContentStreamImpl(cmisFile.getFileName(), null, cmisFile.getMimeType(), cmisFile.getInputStream());
		uuidFolder.createDocument(newDocProps, contentStream, VersioningState.NONE, null, null, null, session.getDefaultContext());
	}

	@Override
	public void updateDescriptorFile(String tenantId, String uuid, CMISEvidenceFile cmisFile) {
		Session session = createSession();
		Folder multitenantRootFolder = (Folder) session.getObjectByPath("/" + tenantId);
		Folder uuidFolder = getUuidFolder(session, uuid, multitenantRootFolder, repoFolderNestingLevels, true);
		for (CmisObject cmisObject : uuidFolder.getChildren()) {
			if (cmisObject.getType().getId().equals(CMISEvidenceFile.PROPERTY_DOC_ID)) {
				Document doc = (Document) cmisObject;
				if (doc.getName().equals(cmisFile.getFileName())) {
					Map<String, String> newDocProps = new HashMap<String, String>();
					newDocProps.put(CMISEvidenceFile.PROPERTY_TRANSACTION_STATUS, cmisFile.getTransactionStatus());
					doc.updateProperties(newDocProps);
					// Recover mime type from stored content
					ContentStream contentStream = new ContentStreamImpl(cmisFile.getFileName(), null, doc.getContentStreamMimeType(), cmisFile.getInputStream());
					doc.setContentStream(contentStream, true);
				} else {
					log.warn("Found unexpected CMIS object type in descriptor folder: " + cmisObject.getName() + " - " + cmisObject.getType().getId() + 
							" (expected " + cmisFile.getFileName() + ")");
				}
			}
		}
		
	}

	@Override
	public void renamePurgeableUuidFolder() {
		
		Session session = createSession();
		Folder multitenantRootFolder = getMultitenantRootFolder(session, false);
		Folder uuidFolder = getPurgableUuidFolder(session, sessionResolver.resolveCurrentSessionIdentifier(), multitenantRootFolder, repoFolderNestingLevels, false);
		Map<String, String> newFolderProps = new HashMap<String, String>();
		newFolderProps.put(PropertyIds.NAME, sessionResolver.resolveCurrentSessionIdentifier());
		newFolderProps.put(PropertyIds.CHANGE_TOKEN, CREATION_TOKEN);
		
		// https://issues.apache.org/jira/browse/CMIS-530?page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel
		try {
		    uuidFolder.updateProperties(newFolderProps, true);
		} catch (CmisObjectNotFoundException confe) {
			session.clear();
			getUuidFolder(session, sessionResolver.resolveCurrentSessionIdentifier(), multitenantRootFolder, repoFolderNestingLevels, false);
		}
		
	}
	
	@Override
	// Asynchronous invocation - tenant and uuid required (!)
	public void removePurgeableResources(String tenantId, String uuid) {
		Session session = createSession();
		Folder multitenantRootFolder = (Folder) session.getObjectByPath("/" + tenantId);
		Folder uuidFolder = getPurgableUuidFolder(session, uuid, multitenantRootFolder, repoFolderNestingLevels, false);
		uuidFolder.deleteTree(true, UnfileObject.DELETE, false);
	}
	
	@Override
	public void removeResource(String uuid) {
		Session session = createSession();
		Folder multitenantRootFolder = (Folder) session.getObjectByPath("/" + multiTenantContextResolver.resolveCurrentTenantIdentifier());
		Folder uuidFolder = getUuidFolder(session, uuid, multitenantRootFolder, repoFolderNestingLevels, false);
		uuidFolder.deleteTree(true, UnfileObject.DELETE, false);
	}
	
	@Override
	// Asynchronous invocation - tenant and uuid required (!)
	public CMISEvidenceFile getDescriptorFile(String tenantId, String uuid, String descriptorFileName) {
		Session session = createSession();
		Folder multitenantRootFolder = (Folder) session.getObjectByPath("/" + tenantId);
		return getDescriptorFile(multitenantRootFolder, session, uuid, descriptorFileName);
	}
	
	@Override
	public CMISEvidenceFile getDescriptorFile(String uuid, String descriptorFileName) {
		Session session = createSession();
		Folder multitenantRootFolder = (Folder) session.getObjectByPath("/" + multiTenantContextResolver.resolveCurrentTenantIdentifier());
		return getDescriptorFile(multitenantRootFolder, session, uuid, descriptorFileName);
	}
	
	private CMISEvidenceFile getDescriptorFile(Folder multitenantRootFolder, Session session, String uuid, String descriptorFileName) {
		Folder uuidFolder = getUuidFolder(session, uuid, multitenantRootFolder, repoFolderNestingLevels, false);
		CMISEvidenceFileImpl descriptorFile = new CMISEvidenceFileImpl();
		for (CmisObject cmisObject : uuidFolder.getChildren()) {
			if (cmisObject.getType().getId().equals(CMISEvidenceFile.PROPERTY_DOC_ID)) {
				Document doc = (Document) cmisObject;
				if (doc.getName().equals(descriptorFileName)) {
					descriptorFile.setFileName(doc.getName());
					descriptorFile.setMimeType(doc.getContentStreamMimeType());
					descriptorFile.setInputStream(doc.getContentStream().getStream());
					if (doc.getProperty(CMISEvidenceFile.PROPERTY_TRANSACTION_ID) != null && !doc.getProperty(CMISEvidenceFile.PROPERTY_TRANSACTION_ID).toString().equals("")) {
					    descriptorFile.setTransactionId(doc.getProperty(CMISEvidenceFile.PROPERTY_TRANSACTION_ID).toString());
					} else {
						descriptorFile.setTransactionId("");
					}
					descriptorFile.setTransactionStatus(doc.getProperty(CMISEvidenceFile.PROPERTY_TRANSACTION_STATUS).toString());
				} else {
					log.warn("Found unexpected CMIS object type in descriptor folder: " + cmisObject.getName() + " - " + cmisObject.getType().getId() + 
							" (expected " + descriptorFileName + ")");
				}
			}
		}
		return descriptorFile;
	}
	
	@Override
	public List<CMISFile> getContentFiles(String uuid) {
		Session session = createSession();
		Folder multitenantRootFolder = (Folder) session.getObjectByPath("/" + multiTenantContextResolver.resolveCurrentTenantIdentifier());
		Folder uuidFolder = getUuidFolder(session, uuid, multitenantRootFolder, repoFolderNestingLevels, false);
		Folder contentsFolder = getFolderByPath(session, CONTENT_FOLDER_NAME, uuidFolder, true);
		List<CMISFile> cmisFiles = new ArrayList<CMISFile>();
		for (CmisObject cmisObject : contentsFolder.getChildren()) {
			if (cmisObject.getType().isBaseType() && cmisObject.getType().getId().equals(CMIS_DOCUMENT)) {
				Document doc = (Document) cmisObject;
				CMISFileImpl file = new CMISFileImpl();
				file.setFileName(doc.getName());
				file.setMimeType(doc.getContentStreamMimeType());
				file.setInputStream(doc.getContentStream().getStream());
				cmisFiles.add(file);
			} else {
				log.warn("Found unexpected CMIS object type in contents folder: " + cmisObject.getName() + " - " +cmisObject.getType().getId());
			}
		}
		return cmisFiles;
	}
	
	@Override
	public void updateMimeTypeOnPurgeable(CMISFile cmisFile, String mimeType) {
		Session session = createSession();
		Folder multitenantRootFolder = getMultitenantRootFolder(session, true);
		Folder uuidFolder = getPurgableUuidFolder(session, sessionResolver.resolveCurrentSessionIdentifier(), multitenantRootFolder, repoFolderNestingLevels, true);
		Folder contentsFolder = getFolderByPath(session, CONTENT_FOLDER_NAME, uuidFolder, true);
		Document document = (Document) session.getObjectByPath ("/" + contentsFolder.getPath() + "/" + cmisFile.getFileName());
		Map<String, String> newDocProps = new HashMap<String, String>();
		newDocProps.put(PropertyIds.CONTENT_STREAM_MIME_TYPE, mimeType);
		document.updateProperties(newDocProps);
	}

	@Override
	public List<String> getListIds() {
		Session session = createSession();
		Folder multitenantRootFolder = getMultitenantRootFolder(session, false);
		List<Folder> parentFolders = new ArrayList<Folder>();
		parentFolders.add(multitenantRootFolder);
		for (int i = 0; i < repoFolderNestingLevels; i++) {
			List<Folder> currentParentFolders = new ArrayList<Folder>();
			for (Folder folder : parentFolders) {
				for (CmisObject child : folder.getChildren()) {
					if (child.getType().isBaseType() && child.getType().getId().equals(CMIS_FOLDER)) {
						currentParentFolders.add((Folder)child);
					}
				}
				parentFolders = currentParentFolders;
			}
		}
		List<String> listIds = new ArrayList<String>();
		for (Folder folder : parentFolders) {
			for (CmisObject child : folder.getChildren()) {
				if (child.getType().isBaseType() && child.getType().getId().equals(CMIS_FOLDER)) {
					listIds.add(child.getName());
				}
			}
		}
		Collections.sort(listIds);
		return listIds;
	}
	
	private Session createSession() {
		SessionFactory f = SessionFactoryImpl.newInstance();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(SessionParameter.USER, user);
		parameters.put(SessionParameter.PASSWORD, password);
		parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameters.put(SessionParameter.ATOMPUB_URL, urlCmisServer);
		parameters.put(SessionParameter.REPOSITORY_ID, repository);
		return f.createSession(parameters);
	}
	
	private Folder getMultitenantRootFolder(Session session, boolean createIfNotExists) {
		Folder multitenantRootFolder = null;
		try {
			multitenantRootFolder = (Folder) session.getObjectByPath("/" + multiTenantContextResolver.resolveCurrentTenantIdentifier());
		} catch (CmisObjectNotFoundException confe) {
			if (createIfNotExists) {
				Map<String, String> tenantFolderProps = new HashMap<String, String>();
				tenantFolderProps.put(PropertyIds.OBJECT_TYPE_ID, CMIS_FOLDER);
				tenantFolderProps.put(PropertyIds.NAME, multiTenantContextResolver.resolveCurrentTenantIdentifier());
				multitenantRootFolder = session.getRootFolder().createFolder(tenantFolderProps, null, null, null, session.getDefaultContext());
			}
		}
		if (multitenantRootFolder == null) {
			throw new RuntimeException("Folder " + multiTenantContextResolver.resolveCurrentTenantIdentifier() + " not found on CMIS root path");
		}
		return multitenantRootFolder;
	}
	
	private Folder getPurgableUuidFolder(Session session, String uuid, Folder multitenantRootFolder, Integer nestingLevels, boolean createIfNotExists) {
		
		int from = 0;
		Folder parentFolder = multitenantRootFolder;
		for (int i = 0; i < nestingLevels; i++) {
			String folderName = uuid.substring(from, from + NESTING_FOLDERS_NAME_SIZE);
			parentFolder = getFolderByPath(session, folderName, parentFolder, createIfNotExists);
			from = from + NESTING_FOLDERS_NAME_SIZE;
		}
		
		Folder folder = getFolderByPath(session, CMISClient.PURGABLE_FOLDER_PREFIX + uuid, parentFolder, createIfNotExists); 
		if (folder == null) {
			throw new RuntimeException("Folder " + uuid + " not found on CMIS path " + multitenantRootFolder.getPath());
		}
		return folder;
		
	}

	private Folder getUuidFolder(Session session, String uuid, Folder multitenantRootFolder, Integer nestingLevels, boolean createIfNotExists) {
		
		int from = 0;
		Folder parentFolder = multitenantRootFolder;
		for (int i = 0; i < nestingLevels; i++) {
			String folderName = uuid.substring(from, from + NESTING_FOLDERS_NAME_SIZE);
			parentFolder = getFolderByPath(session, folderName, parentFolder, createIfNotExists);
			from = from + NESTING_FOLDERS_NAME_SIZE;
		}
		
		Folder folder = getFolderByPath(session, uuid, parentFolder, createIfNotExists);
		if (folder == null) {
			throw new RuntimeException("Folder " + uuid + " not found on CMIS path " + multitenantRootFolder.getPath());
		}
		return folder;
		
	}
	
	private Folder getFolderByPath(Session session, String folderPath, Folder parentFolder, boolean createIfNotExists) {
		Folder folder = null;
		try {
			folder = (Folder) session.getObjectByPath ("/" + parentFolder.getPath() + "/" + folderPath);
		} catch (CmisObjectNotFoundException confe) {
			if (createIfNotExists) {
				Map<String, String> uuidFolderProps = new HashMap<String, String>();
				uuidFolderProps.put(PropertyIds.OBJECT_TYPE_ID, CMIS_FOLDER);
				uuidFolderProps.put(PropertyIds.NAME, folderPath);
				folder = parentFolder.createFolder(uuidFolderProps, null, null, null, session.getDefaultContext());
			}
		}
		return folder;
	}
	
}
