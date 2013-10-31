package org.icearchiva.lta.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.activation.DataHandler;

import org.icearchiva.cmis.client.CMISClient;
import org.icearchiva.cmis.client.CMISEvidenceFile;
import org.icearchiva.cmis.client.CMISFile;
import org.icearchiva.commons.session.ICurrentSessionIdentifierResolver;
import org.icearchiva.commons.tenancy.context.ICurrentTenantIdentifierResolver;
import org.icearchiva.lta.service.model.AIPArchiveRequestImpl;
import org.icearchiva.lta.service.model.AIPPackageImpl;
import org.icearchiva.lta.service.model.CMISEvidenceFileImpl;
import org.icearchiva.lta.service.model.CMISFileImpl;
import org.icearchiva.lta.service.model.FileInfoImpl;
import org.icearchiva.lta.service.model.IAIPFile;
import org.icearchiva.lta.service.model.IAIPMetadata;
import org.icearchiva.lta.service.model.IAIPPackage;
import org.icearchiva.lta.service.model.IDescriptorDetails;
import org.icearchiva.lta.service.model.IESignResponse;
import org.icearchiva.lta.service.model.IFileInfo;
import org.icearchiva.lta.service.model.UserImpl;
import org.icearchiva.lta.ws.v1.ArchiveData;
import org.icearchiva.lta.ws.v1.ArchiveData.Element;
import org.icearchiva.lta.ws.v1.ArchiveRequest;
import org.icearchiva.lta.ws.v1.ArchiveResponse;
import org.icearchiva.lta.ws.v1.DeleteRequest;
import org.icearchiva.lta.ws.v1.DeleteResponse;
import org.icearchiva.lta.ws.v1.Evidence;
import org.icearchiva.lta.ws.v1.ExportRequest;
import org.icearchiva.lta.ws.v1.ExportResponse;
import org.icearchiva.lta.ws.v1.ListIds;
import org.icearchiva.lta.ws.v1.ListIdsRequest;
import org.icearchiva.lta.ws.v1.ListIdsResponse;
import org.icearchiva.lta.ws.v1.MetaData;
import org.icearchiva.lta.ws.v1.MetaItem;
import org.icearchiva.lta.ws.v1.StatusInformation;
import org.icearchiva.lta.ws.v1.StatusNotice;
import org.icearchiva.lta.ws.v1.StatusRequest;
import org.icearchiva.lta.ws.v1.StatusResponse;
import org.icearchiva.lta.ws.v1.VerifyRequest;
import org.icearchiva.lta.ws.v1.VerifyResponse;
import org.icearchiva.utils.InputStreamDataSource;
import org.icearchiva.utils.LTAArchiveInputStream;

public class LtaServiceImpl implements LtaService {
	
	private static final String ENTITY_TYPE = "EXTERNAL";
	
	private ICurrentSessionIdentifierResolver sessionResolver;
	private ICurrentTenantIdentifierResolver multiTenantContextResolver;
	private AIPService aipService;
	private ESignService esignService;
	private SearchService searchService;
	private CMISClient cmisClient;
	
	private void removePurgeableResources(final String tenantId, final String uuid) {
		
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(
				new Runnable() {
		            public void run() {
		            	cmisClient.removePurgeableResources(tenantId, uuid);
		            }
	            }
			);
		executorService.shutdown();
		
	}
    
	@Override
	public ArchiveResponse archiveOperation(ArchiveRequest parameters) {
		
		// TODO · LTAP version control
		parameters.getVersion();

		// Previous transaction identified
		if (parameters.getTransactionId() != null && !parameters.getTransactionId().equals("")) {
			
			List<IAIPPackage> aipPackages = searchService.findByTransactionId(parameters.getTransactionId());

			ArchiveResponse ar = new ArchiveResponse();
			
			if (aipPackages.size() == 1) {
				
				IAIPPackage aipPackage = aipPackages.get(0);
				
		        ar.setReferenceId(aipPackage.getReferenceId());
		        StatusNotice st = new StatusNotice();
		        st.setStatus(StatusInformation.fromValue(aipPackage.getStatus()));
		        if (aipPackage.getStatus().equals(StatusInformation.WAITING.value())) {
			        st.setTransactionIdentifier(parameters.getTransactionId());
		        }			        	
		        ar.setStatus(st);

			} else {
				
		        StatusNotice st = new StatusNotice();
		        st.setStatus(StatusInformation.REJECTION);
		        st.setErrorInformation("TransactionId " + parameters.getTransactionId() + " found " + aipPackages.size() + " times on repository (expected 1)");
		        ar.setStatus(st);
		        
			}
			
	        return ar;
			
		}
		
		// No previous transaction identified
		try {
		
			// Store attachments on CMIS server
			List<IFileInfo> files = new ArrayList<IFileInfo>();
			if (parameters.getData() != null) {
		        List<Element> elements = parameters.getData().getElement();
		        if (elements != null) {
			        for (Element element : elements) {
			        	
			        	LTAArchiveInputStream ltaIs = new LTAArchiveInputStream(element.getData().getInputStream());
			        	
						CMISFileImpl cmisFile = new CMISFileImpl();
			        	cmisFile.setFileName(element.getData().getName());
			        	cmisFile.setInputStream(ltaIs);
			        	cmisFile.setMimeType(element.getData().getContentType());
			        	cmisClient.uploadContentToPurgeableFolder(cmisFile);
			        	
			        	FileInfoImpl fileInfo = new FileInfoImpl();
			        	fileInfo.setName(cmisFile.getFileName());
			        	String mimeType = cmisFile.getMimeType();
			        	if (ltaIs.getMimeType() != null && !ltaIs.getMimeType().equals("")) {
			        		mimeType = ltaIs.getMimeType();
			        		cmisClient.updateMimeTypeOnPurgeable(cmisFile, mimeType);
			        	}
			        	fileInfo.setMimeType(mimeType);
			        	fileInfo.setHash(ltaIs.getHash());
			        	fileInfo.setHashType(LTAArchiveInputStream.HASH_ALGORITHM);
			        	fileInfo.setSize(ltaIs.getSize());
			        	files.add(fileInfo);
			        	
			        }
		        }
	        }
			
			// AIP building
			UserImpl user = new UserImpl();
			user.setEntity(multiTenantContextResolver.resolveCurrentTenantIdentifier());
			user.setName(multiTenantContextResolver.resolveCurrentTenantIdentifier());
			user.setType(ENTITY_TYPE);
			AIPArchiveRequestImpl aipArchive = new AIPArchiveRequestImpl();
			aipArchive.setAipId(sessionResolver.resolveCurrentSessionIdentifier());
			aipArchive.setUser(user);
			aipArchive.setArchiveRequest(parameters);
			aipArchive.setFileInfoList(files);
			aipArchive.setContentsPath(cmisClient.getContentsPath());
			InputStream metsFile = aipService.getPackage(aipArchive);
			
			// Request XAdES-A signature
			IESignResponse esignResponse = esignService.getXAdES_A(metsFile);
			
			// Store AIP file on CMIS server 
			CMISEvidenceFileImpl cmisFile = new CMISEvidenceFileImpl();
			cmisFile.setFileName(aipService.getFileName());
			cmisFile.setMimeType(aipService.getMimeType());
			cmisFile.setInputStream(esignResponse.getSignedContent());
			cmisFile.setTransactionId(esignResponse.getTransactionId());
			cmisFile.setTransactionStatus(esignResponse.getTransactionStatus());
			cmisClient.uploadDescriptorToPurgeableFolder(cmisFile);
			
			// Results
			ArchiveResponse ar = new ArchiveResponse();
	        ar.setReferenceId(sessionResolver.resolveCurrentSessionIdentifier());
	        StatusNotice st = new StatusNotice();
	        if (cmisFile.getTransactionStatus().equals(CMISEvidenceFile.TRANSACTION_STATUS_PENDING)) {
		        st.setStatus(StatusInformation.WAITING);
		        st.setTransactionIdentifier(cmisFile.getTransactionId());
	        } else if (cmisFile.getTransactionStatus().equals(CMISEvidenceFile.TRANSACTION_STATUS_GRANTED)) {
		        st.setStatus(StatusInformation.GRANTED);
	        } else if (cmisFile.getTransactionStatus().equals(CMISEvidenceFile.TRANSACTION_STATUS_REJECTED)) {
		        st.setStatus(StatusInformation.REJECTION);
	        }
	        ar.setStatus(st);
	        
	        // Index operation
	        AIPPackageImpl iAipPackage = new AIPPackageImpl();
	        iAipPackage.setTenantId(multiTenantContextResolver.resolveCurrentTenantIdentifier());
	        iAipPackage.setReferenceId(ar.getReferenceId());
	        iAipPackage.setTransactionId(ar.getStatus().getTransactionIdentifier());
	        iAipPackage.setTransactionDate(new Date());
	        iAipPackage.setStatus(ar.getStatus().getStatus().value());
	        if (esignResponse.getStampingDate() != null) {
	        	iAipPackage.setStampingDate(esignResponse.getStampingDate());
	        }
	        searchService.addToIndex(iAipPackage);

			// Rename purgeable folder to definitive UUID
		    cmisClient.renamePurgeableUuidFolder();
		    
	        return ar;
        
		} catch (Exception e) {
			removePurgeableResources(multiTenantContextResolver.resolveCurrentTenantIdentifier(),
					sessionResolver.resolveCurrentSessionIdentifier());
			throw new RuntimeException(e);
		}
        
    }
    
	@Override
	public ExportResponse exportOperation(ExportRequest parameters) {
		
		// TODO · Transaction control
		parameters.getTransactionId();
		// TODO · LTAP version control
		parameters.getVersion();
		
		String uuid = parameters.getReferenceId();
		
		// Find AIP file
		CMISEvidenceFile descriptor = cmisClient.getDescriptorFile(uuid, aipService.getFileName());
		InputStream descriptorXMLInfo = esignService.extractXMLInfoFromSignature(descriptor.getInputStream());
		IDescriptorDetails details = aipService.getDetails(descriptorXMLInfo);
		// Restore exhausted stream
		descriptor = cmisClient.getDescriptorFile(uuid, aipService.getFileName());
		
		ArchiveData archiveData = new ArchiveData();
		List<CMISFile> cmisFiles = cmisClient.getContentFiles(uuid);
		for(CMISFile cmisFile : cmisFiles) {
			Element element = new Element();
			DataHandler content = 
					new DataHandler(new InputStreamDataSource(cmisFile.getInputStream(), cmisFile.getFileName(), cmisFile.getMimeType()));
			MetaData metadata = new MetaData();
			IAIPMetadata meta = details.getMetadata(cmisFile.getFileName());
			for (Entry<String, List<String>> m : meta.getMetaItems().entrySet()) {
				MetaItem mi = new MetaItem();
				mi.setType(m.getKey());
				for (String value : m.getValue()) {
					mi.getValues().add(value);
				}
				metadata.getMetaItem().add(mi);
			}
			element.setData(content);
			element.setMetaData(metadata);
			archiveData.getElement().add(element);
		}
		
		Evidence evidence = new Evidence();
		evidence.setData(new DataHandler(new InputStreamDataSource(descriptor.getInputStream(), descriptor.getFileName(), descriptor.getMimeType())));

		ExportResponse er = new ExportResponse();
		er.setPolicyId(Integer.parseInt(details.getPolicyId()));
		er.setReferenceId(details.getReferenceId());
		er.setData(archiveData);
		er.setEvidence(evidence);
		StatusNotice st = new StatusNotice();
		st.setStatus(StatusInformation.GRANTED);
		er.setStatus(st);
		
		return er;
	}

	@Override
	public ListIdsResponse listIdsOperation(ListIdsRequest parameters)  {
		
		// TODO · Transaction control
		parameters.getTransactionId();
		// TODO · LTAP version control
		parameters.getVersion();
		
		List<String> requestedListIds = cmisClient.getListIds();
		
		// Remove purgeable resources
		for (Iterator<String> iterator = requestedListIds.iterator(); iterator.hasNext();) {
			String id = iterator.next();
			if (id.startsWith(CMISClient.PURGABLE_FOLDER_PREFIX)) {
				iterator.remove();
			}
		}
		
		ListIdsResponse lir = new ListIdsResponse();
		ListIds listIds = new ListIds();
		listIds.getId().addAll(requestedListIds);
		lir.setListIds(listIds);
		return lir;
	}
	
	@Override
	public VerifyResponse verifyOperation(VerifyRequest parameters)  {
		
		// TODO · Transaction control
		parameters.getTransactionId();
		// TODO · LTAP version control
		parameters.getVersion();
		
		String uuid = parameters.getReferenceId();
		
		// Find AIP file
		CMISFile descriptor = cmisClient.getDescriptorFile(uuid, aipService.getFileName());
		InputStream descriptorXMLInfo = esignService.extractXMLInfoFromSignature(descriptor.getInputStream());
		IDescriptorDetails details = aipService.getDetails(descriptorXMLInfo);
		
		// Recover CMISFiles
		List<CMISFile> cmisFiles = cmisClient.getContentFiles(uuid);
		Map<String, CMISFile> cmisFilesMap = new HashMap<String, CMISFile>();
		for(CMISFile cmisFile : cmisFiles) {
			cmisFilesMap.put(cmisFile.getFileName(), cmisFile);
		}
		
		for (IAIPFile file : details.getFiles()) {
			
			String fileName = file.getFileName();
			CMISFile cmisFile = cmisFilesMap.get(fileName);
			if (cmisFile == null) {
				throw new RuntimeException("File " + fileName + " not found on " + multiTenantContextResolver.resolveCurrentTenantIdentifier() + 
						" CMIS respository for " + uuid + " package");
			}
			
			LTAArchiveInputStream lfis = new LTAArchiveInputStream(cmisFile.getInputStream(), file.getChecksumType());
			try {
				consumeInputStream(lfis);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
			String checksum = file.getChecksum();
			if (!lfis.getHash().equals(checksum)) {
				throw new RuntimeException("File " + fileName + " found on " + multiTenantContextResolver.resolveCurrentTenantIdentifier() + 
						" CMIS respository has " + lfis.getHash() + " checksum (expected " +  checksum + ")");
			}
			Long size = file.getSize();
			if (!lfis.getSize().equals(size)) {
				throw new RuntimeException("File " + fileName + " found on " + multiTenantContextResolver.resolveCurrentTenantIdentifier() + 
						" CMIS respository has " + lfis.getSize() + " size (expected " +  size + ")");
			}
			String mimeType = file.getMimeType();
			if (!lfis.getMimeType().equals("")) {
				if (!lfis.getMimeType().equals(mimeType)) {
					throw new RuntimeException("File " + fileName + " found on " + multiTenantContextResolver.resolveCurrentTenantIdentifier() + 
							" CMIS respository has " + lfis.getMimeType() + " mimetype (expected " +  mimeType + ")");
				}
			}
			
		}
		
		// Signature validation
		descriptor = cmisClient.getDescriptorFile(uuid, aipService.getFileName());
		if (!esignService.verifyXAdES_A(descriptor.getInputStream())) {
			throw new RuntimeException("Evidence for package " + uuid + " can't be verified!");
		}
		
		StatusNotice st = new StatusNotice();
		st.setStatus(StatusInformation.GRANTED);
		VerifyResponse vr = new VerifyResponse();
		vr.setStatus(st);
		return vr;
	}

	@Override
	public DeleteResponse deleteOperation(DeleteRequest parameters) {
		
		// TODO · Transaction control
		parameters.getTransactionId();
		// TODO · LTAP version control
		parameters.getVersion();
		
        // Index operation
        searchService.deleteFromIndex(parameters.getReferenceId());
        
        // CMIS operation
		String uuid = parameters.getReferenceId();
		cmisClient.removeResource(uuid);
		
		DeleteResponse dr = new DeleteResponse();
		StatusNotice st = new StatusNotice();
		st.setStatus(StatusInformation.GRANTED);
		dr.setStatus(st);
		
		return dr;
		
	}
	
	@Override
	public StatusResponse statusOperation(StatusRequest parameters)  {
		
		List<IAIPPackage> aipPackages = searchService.findByTransactionId(parameters.getTransactionId());

		StatusResponse sr = new StatusResponse();
		
		if (aipPackages.size() == 1) {
			
			IAIPPackage aipPackage = aipPackages.get(0);
			
	        StatusNotice st = new StatusNotice();
	        st.setStatus(StatusInformation.fromValue(aipPackage.getStatus()));
	        if (aipPackage.getStatus().equals(StatusInformation.WAITING)) {
		        st.setTransactionIdentifier(parameters.getTransactionId());
	        }			        	
	        sr.setStatus(st);

		} else {
			
	        StatusNotice st = new StatusNotice();
	        st.setStatus(StatusInformation.REJECTION);
	        st.setErrorInformation("TransacionId " + parameters.getTransactionId() + " found " + aipPackages.size() + " times on repository (expected 1)");
	        sr.setStatus(st);
	        
		}
		
        return sr;
			
	}
	
    @Override
    public StatusInformation checkAsynchronousTransaction(IAIPPackage aipPackage) {
    	
    	IESignResponse esignResponse = esignService.checkAsyncTransaction(aipPackage.getTransactionId());
    	
        StatusInformation st;
    	if (esignResponse.getTransactionStatus().equals(CMISEvidenceFile.TRANSACTION_STATUS_PENDING)) {
	        st = StatusInformation.WAITING;
        } else if (esignResponse.getTransactionStatus().equals(CMISEvidenceFile.TRANSACTION_STATUS_GRANTED)) {
	        st = StatusInformation.GRANTED;
        } else if (esignResponse.getTransactionStatus().equals(CMISEvidenceFile.TRANSACTION_STATUS_REJECTED)) {
	        st = StatusInformation.REJECTION;
        } else {
        	st = StatusInformation.MORE;
        }
    	
    	if (esignResponse.getTransactionStatus().equals(CMISEvidenceFile.TRANSACTION_STATUS_GRANTED)) {
        	
            // Update descriptor file with long term signature
    		CMISEvidenceFileImpl cmisFile = new CMISEvidenceFileImpl();
    		cmisFile.setFileName(aipService.getFileName());
    		cmisFile.setInputStream(esignResponse.getSignedContent());
    		cmisFile.setTransactionId(esignResponse.getTransactionId());
    		cmisFile.setTransactionStatus(esignResponse.getTransactionStatus());
    		cmisClient.updateDescriptorFile(aipPackage.getTenantId(), aipPackage.getReferenceId(), cmisFile);
    		
    		// Index updated fields
    		AIPPackageImpl aipPackageUpdated = new AIPPackageImpl();
    		aipPackageUpdated.setReferenceId(aipPackage.getReferenceId());
    		aipPackageUpdated.setStatus(StatusInformation.GRANTED.value());
    		aipPackageUpdated.setTenantId(aipPackage.getTenantId());
    		aipPackageUpdated.setTransactionDate(aipPackage.getTransactionDate());
    		aipPackageUpdated.setTransactionId(aipPackage.getTransactionId());
	        if (esignResponse.getStampingDate() != null) {
	        	aipPackageUpdated.setStampingDate(esignResponse.getStampingDate());
	        }
    		searchService.update(aipPackageUpdated);
    		
	        return st;
	        
        } else {
        	
        	return st;
        	
        }
        
    }
    
    @Override
    public StatusInformation restamp(IAIPPackage aipPackage) {
    	
		CMISFile descriptor = cmisClient.getDescriptorFile(aipPackage.getTenantId(), aipPackage.getReferenceId(), aipService.getFileName());
		IESignResponse esignResponse = esignService.addTimeStamp(descriptor.getInputStream());
		
        // Update descriptor file with long term signature
		CMISEvidenceFileImpl cmisFile = new CMISEvidenceFileImpl();
		cmisFile.setFileName(aipService.getFileName());
		cmisFile.setInputStream(esignResponse.getSignedContent());
		cmisFile.setTransactionStatus(StatusInformation.GRANTED.value());
		cmisClient.updateDescriptorFile(aipPackage.getTenantId(), aipPackage.getReferenceId(), cmisFile);
		
		// Index updated fields
		AIPPackageImpl aipPackageUpdated = new AIPPackageImpl();
		aipPackageUpdated.setReferenceId(aipPackage.getReferenceId());
		aipPackageUpdated.setStatus(StatusInformation.GRANTED.value());
		aipPackageUpdated.setTenantId(aipPackage.getTenantId());
		aipPackageUpdated.setTransactionDate(aipPackage.getTransactionDate());
		aipPackageUpdated.setTransactionId(aipPackage.getTransactionId());
        if (esignResponse.getStampingDate() != null) {
        	aipPackageUpdated.setStampingDate(esignResponse.getStampingDate());
        }
		searchService.update(aipPackageUpdated);
    	
    	return StatusInformation.GRANTED;
    	
    }
    
	
	private final int BYTE_BUFFER_SIZE = 65535;
	private void consumeInputStream(InputStream is) throws Exception {
		byte[] data = new byte[BYTE_BUFFER_SIZE];
		while ((is.read(data, 0, data.length)) != -1) {}
		is.close();
	}

	@Override
	public void setAipService(AIPService aipService) {
		this.aipService = aipService;
	}

	@Override
	public void setEsignService(ESignService esignService) {
		this.esignService = esignService;
	}

	@Override
	public AIPService getAipService() {
		return aipService;
	}

	@Override
	public ESignService getEsignService() {
		return esignService;
	}

	public CMISClient getCmisClient() {
		return cmisClient;
	}

	public void setCmisClient(CMISClient cmisClient) {
		this.cmisClient = cmisClient;
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

	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

}
