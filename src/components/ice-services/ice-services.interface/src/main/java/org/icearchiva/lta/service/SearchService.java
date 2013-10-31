package org.icearchiva.lta.service;

import java.util.Date;
import java.util.List;

import org.icearchiva.lta.service.model.IAIPPackage;

public interface SearchService {
	
    public void addToIndex(IAIPPackage iAipPackage);

    public void deleteFromIndex(String referenceId);

    public void update(IAIPPackage iAipPackage);
    
    public List<IAIPPackage> findByTransactionId(String transactionId);
    
    public List<IAIPPackage> findByStatusAndDate(String status, Date date);
    
    public List<IAIPPackage> findByReferenceId(String referenceId);
    
    public List<IAIPPackage> findByStampingDate(Date date);

}
