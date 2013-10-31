package org.icearchiva.lta.task;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class RestampingJob extends QuartzJobBean {
	
	private RestampingTask restampingTask;
 
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		restampingTask.execute();
	}

	public RestampingTask getRestampingTask() {
		return restampingTask;
	}

	public void setRestampingTask(RestampingTask restampingTask) {
		this.restampingTask = restampingTask;
	}
	
}