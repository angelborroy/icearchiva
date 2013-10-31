package org.icearchiva.lta.task;

import java.util.Calendar;
import java.util.List;

import org.icearchiva.lta.service.LtaService;
import org.icearchiva.lta.service.SearchService;
import org.icearchiva.lta.service.model.IAIPPackage;
import org.icearchiva.lta.ws.v1.StatusInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitingSignaturesTask {
	
	private static final Logger logger = LoggerFactory.getLogger(WaitingSignaturesTask.class);
	
	private static int GRACE_PERIOD_DAYS = 1;
 
	private SearchService searchService;
	private LtaService ltaService;
	
	protected void execute() {
		
		if (logger.isInfoEnabled()) {
			logger.info("execute()"); //$NON-NLS-1$
		}
		
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, -GRACE_PERIOD_DAYS);
		
		List<IAIPPackage> waitingSignaturesList = searchService.findByStatusAndDate(StatusInformation.WAITING.value(), now.getTime());
		for (IAIPPackage signature : waitingSignaturesList) {
			try {
				StatusInformation st = ltaService.checkAsynchronousTransaction(signature);
				if (logger.isInfoEnabled()) {
					logger.info("Waiting task " + signature.getReferenceId() + " processed with result st={}", st);
				}
			} catch (RuntimeException re) {
				logger.warn("Waiting task " + signature.getReferenceId() + " not completed error={}", re.getMessage());
				re.printStackTrace(System.err);
			}
		}
		
	}
	
	public SearchService getSearchService() {
		return searchService;
	}
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public LtaService getLtaService() {
		return ltaService;
	}

	public void setLtaService(LtaService ltaService) {
		this.ltaService = ltaService;
	}
	
}