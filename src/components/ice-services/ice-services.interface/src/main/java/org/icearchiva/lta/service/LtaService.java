package org.icearchiva.lta.service;

import org.icearchiva.lta.service.model.IAIPPackage;
import org.icearchiva.lta.ws.v1.ArchiveRequest;
import org.icearchiva.lta.ws.v1.ArchiveResponse;
import org.icearchiva.lta.ws.v1.DeleteRequest;
import org.icearchiva.lta.ws.v1.DeleteResponse;
import org.icearchiva.lta.ws.v1.ExportRequest;
import org.icearchiva.lta.ws.v1.ExportResponse;
import org.icearchiva.lta.ws.v1.ListIdsRequest;
import org.icearchiva.lta.ws.v1.ListIdsResponse;
import org.icearchiva.lta.ws.v1.StatusInformation;
import org.icearchiva.lta.ws.v1.StatusRequest;
import org.icearchiva.lta.ws.v1.StatusResponse;
import org.icearchiva.lta.ws.v1.VerifyRequest;
import org.icearchiva.lta.ws.v1.VerifyResponse;


public interface LtaService {
	
	public void setAipService(AIPService aipService);
	
	public AIPService getAipService();
	
	public void setEsignService(ESignService esignService);
	
	public ESignService getEsignService();

    public ArchiveResponse archiveOperation(ArchiveRequest parameters);

    public DeleteResponse deleteOperation(DeleteRequest parameters);

    public ExportResponse exportOperation(ExportRequest parameters);

    public ListIdsResponse listIdsOperation(ListIdsRequest parameters);

    public StatusResponse statusOperation(StatusRequest parameters);

    public VerifyResponse verifyOperation(VerifyRequest parameters);
    
    public StatusInformation checkAsynchronousTransaction(IAIPPackage aipPackage);
    
    public StatusInformation restamp(IAIPPackage aipPackage);
    
}
