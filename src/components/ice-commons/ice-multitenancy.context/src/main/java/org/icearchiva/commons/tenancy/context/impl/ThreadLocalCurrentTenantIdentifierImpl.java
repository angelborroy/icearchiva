package org.icearchiva.commons.tenancy.context.impl;

import org.icearchiva.commons.tenancy.context.ICurrentTenantIdentifierResolver;
import org.icearchiva.commons.tenancy.context.ICurrentTenantIdentifierSetter;

public class ThreadLocalCurrentTenantIdentifierImpl implements ICurrentTenantIdentifierResolver, ICurrentTenantIdentifierSetter {

    private static ThreadLocal<String> currentTenantId = new ThreadLocal<String>();
    
    public void setCurrentTenantId(String tenantId){
        currentTenantId.set(tenantId);
    }

    public String resolveCurrentTenantIdentifier() {
        return currentTenantId.get();
    }
}