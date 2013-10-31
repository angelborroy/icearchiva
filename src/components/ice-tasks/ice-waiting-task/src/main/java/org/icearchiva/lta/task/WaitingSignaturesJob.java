package org.icearchiva.lta.task;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class WaitingSignaturesJob extends QuartzJobBean {
	
	private WaitingSignaturesTask waitingSignaturesTask;
 
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		waitingSignaturesTask.execute();
	}

	public WaitingSignaturesTask getWaitingSignaturesTask() {
		return waitingSignaturesTask;
	}

	public void setWaitingSignaturesTask(WaitingSignaturesTask waitingSignaturesTask) {
		this.waitingSignaturesTask = waitingSignaturesTask;
	}
	
}