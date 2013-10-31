package org.icearchiva.commons.tenancy.logger;

import org.slf4j.Logger;

public interface IMultiTenancyLoggerResolver {
	
	public Logger resolveCurrentTenantLogger();

}
