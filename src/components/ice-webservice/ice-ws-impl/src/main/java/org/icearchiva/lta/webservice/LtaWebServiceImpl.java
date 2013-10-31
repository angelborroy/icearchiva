package org.icearchiva.lta.webservice;

import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.MTOM;

import org.icearchiva.lta.service.LtaService;
import org.icearchiva.lta.ws.v1.ArchiveRequest;
import org.icearchiva.lta.ws.v1.ArchiveResponse;
import org.icearchiva.lta.ws.v1.DeleteRequest;
import org.icearchiva.lta.ws.v1.DeleteResponse;
import org.icearchiva.lta.ws.v1.ExportRequest;
import org.icearchiva.lta.ws.v1.ExportResponse;
import org.icearchiva.lta.ws.v1.ListIdsRequest;
import org.icearchiva.lta.ws.v1.ListIdsResponse;
import org.icearchiva.lta.ws.v1.StatusRequest;
import org.icearchiva.lta.ws.v1.StatusResponse;
import org.icearchiva.lta.ws.v1.VerifyRequest;
import org.icearchiva.lta.ws.v1.VerifyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@WebService(portName = "ltaPort", serviceName = "ltaService", targetNamespace = "http://icearchiva.org/lta/ws/v1/", endpointInterface = "org.icearchiva.lta.ws.v1.LtaPortType")
@BindingType("http://schemas.xmlsoap.org/wsdl/soap/http")
@MTOM(enabled=true)
public class LtaWebServiceImpl implements ILtaWebService {
	
	private static final Logger log = LoggerFactory.getLogger(LtaWebServiceImpl.class);
	
	private LtaService ltaService;

    public LtaService getLtaService() {
		return ltaService;
	}

	public void setLtaService(LtaService ltaService) {
		this.ltaService = ltaService;
	}

	public ArchiveResponse archiveOperation(ArchiveRequest parameters) {
		if (log.isInfoEnabled()) log.info("ArchiveOperation\tRequest");
		try {
		    ArchiveResponse archiveResponse = ltaService.archiveOperation(parameters);
			if (log.isInfoEnabled()) log.info("ArchiveOperation\tResponse");
		    return archiveResponse;
		} catch (Throwable e) {
			log.error("Archive response fault", e);
			throw new RuntimeException(e.getMessage() + ": " + e.getStackTrace()[0]);
		}
		
	}    	

    public DeleteResponse deleteOperation(DeleteRequest parameters) {
		if (log.isInfoEnabled()) log.info("DeleteOperation\tRequest");
		try {
    	    DeleteResponse deleteResponse = ltaService.deleteOperation(parameters);
			if (log.isInfoEnabled()) log.info("DeleteOperation\tResponse");
			return deleteResponse;
		} catch (Throwable e) {
			log.error("Delete response fault", e);
			throw new RuntimeException(e.getMessage() + ": " + e.getStackTrace()[0]);
		}
    }

    public ExportResponse exportOperation(ExportRequest parameters) {
		if (log.isInfoEnabled()) log.info("ExportOperation\tRequest");
		try {
	    	ExportResponse exportResponse = ltaService.exportOperation(parameters);
			if (log.isInfoEnabled()) log.info("ExportOperation\tResponse");
	    	return exportResponse;
		} catch (Throwable e) {
			log.error("Export response fault", e);
			throw new RuntimeException(e.getMessage() + ": " + e.getStackTrace()[0]);
		}
    }

    public ListIdsResponse listIdsOperation(ListIdsRequest parameters) {
		if (log.isInfoEnabled()) log.info("ListIdsOperation\tRequest");
    	try {
	    	ListIdsResponse listIdsResponse = ltaService.listIdsOperation(parameters);
			if (log.isInfoEnabled()) log.info("ListIdsOperation\tResponse");
	    	return listIdsResponse;
		} catch (Throwable e) {
			log.error("ListIds response fault", e);
			throw new RuntimeException(e.getMessage() + ": " + e.getStackTrace()[0]);
		}
    }

    public StatusResponse statusOperation(StatusRequest parameters) {
		if (log.isInfoEnabled()) log.info("StatusOperation\tRequest");
    	try {
	    	StatusResponse statusResponse = ltaService.statusOperation(parameters);
			if (log.isInfoEnabled()) log.info("StatusOperation\tResponse");
			return statusResponse;
		} catch (Throwable e) {
			log.error("Status response fault", e);
			throw new RuntimeException(e.getMessage() + ": " + e.getStackTrace()[0]);
		}
    }

    public VerifyResponse verifyOperation(VerifyRequest parameters) {
		if (log.isInfoEnabled()) log.info("VerifyOperation\tRequest");
    	try {
    	    VerifyResponse verifyResponse = ltaService.verifyOperation(parameters);
			if (log.isInfoEnabled()) log.info("VerifyOperation\tResponse");
			return verifyResponse;
		} catch (Throwable e) {
			log.error("Verify response fault", e);
			throw new RuntimeException(e.getMessage() + ": " + e.getStackTrace()[0]);
		}
    }

}