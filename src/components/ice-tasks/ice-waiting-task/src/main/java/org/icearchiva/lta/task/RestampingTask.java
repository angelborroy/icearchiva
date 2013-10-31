package org.icearchiva.lta.task;

import java.util.Calendar;
import java.util.List;

import org.icearchiva.lta.service.LtaService;
import org.icearchiva.lta.service.SearchService;
import org.icearchiva.lta.service.model.IAIPPackage;
import org.icearchiva.lta.ws.v1.StatusInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestampingTask {
	
	private static final Logger logger = LoggerFactory.getLogger(RestampingTask.class);
	
	private SearchService searchService;
	private LtaService ltaService;
	
	protected void execute() {
		
		if (logger.isInfoEnabled()) {
			logger.info("execute()"); //$NON-NLS-1$
		}
		
		Calendar now = Calendar.getInstance();
		now.add(Calendar.MONTH, -1);
		
		List<IAIPPackage> toBeRestampedList = searchService.findByStampingDate(now.getTime());
		
		for (IAIPPackage signature : toBeRestampedList) {
			try {
				StatusInformation st = ltaService.restamp(signature);
				if (logger.isInfoEnabled()) {
					logger.info("Restamping task " + signature.getReferenceId() + " processed with result st={}", st);
				}
			} catch (RuntimeException re) {
				logger.warn("Restamping task " + signature.getReferenceId() + " not completed. Error={}", re.getMessage());
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