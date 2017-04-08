package com.karpunets.jaas;

import com.karpunets.dao.utils.oracle.proxy.ProxyFactory;
import com.karpunets.pojo.grants.Grant;

import java.security.Principal;

public class RolePrincipal implements Principal {

    private String name;

    public RolePrincipal(Grant user) {
        super();
        if (user instanceof ProxyFactory.Proxy) {
            this.name = user.getClass().getSuperclass().getSimpleName().toLowerCase();
        } else {
            this.name = user.getClass().getSimpleName().toLowerCase();
        }
    }

    public void setName(String name) {

        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
