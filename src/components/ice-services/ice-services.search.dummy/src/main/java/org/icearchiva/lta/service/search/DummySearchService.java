package org.icearchiva.lta.service.search;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.icearchiva.lta.service.SearchService;
import org.icearchiva.lta.service.model.IAIPPackage;

public class DummySearchService implements SearchService {
	
	private static SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	@Override
	public void addToIndex(IAIPPackage iAipPackage) {
	}

	@Override
	public void deleteFromIndex(String referenceId) {
	}

	public List<IAIPPackage> search(String searchTerms) {
		return null;
	}

	@Override
	public List<IAIPPackage> findByTransactionId(String transactionId) {
		String searchTerms = "transactionId:" + transactionId;
		return search(searchTerms);
	}

	@Override
	public List<IAIPPackage> findByStatusAndDate(String status, Date date) {
		String searchTerms = "status:" + status +" AND transactionDate:[* TO " + sdfUTC.format(date) + "]";
		return search(searchTerms);
	}
	
	@Override
	public List<IAIPPackage> findByStampingDate(Date date) {
		String searchTerms = "stampingDate:[* TO " + sdfUTC.format(date) + "]";
		return search(searchTerms);
	}

	@Override
	public List<IAIPPackage> findByReferenceId(String referenceId) {
		String searchTerms = "referenceId:" + referenceId;
		return search(searchTerms);
	}

	@Override
	public void update(IAIPPackage iAipPackage) {
		addToIndex(iAipPackage);
	}

}
