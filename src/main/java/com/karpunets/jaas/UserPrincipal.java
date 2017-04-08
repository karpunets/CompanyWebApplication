package com.karpunets.jaas;

import com.karpunets.pojo.grants.Grant;

import java.security.Principal;

public class UserPrincipal implements Principal {

    private Grant user;

    public UserPrincipal(Grant user) {
        super();
        this.user = user;
    }

    @Override
    public String getName() {
        return user.getLogin();
    }

    public Grant getUser() {
        return user;
    }

    public void setUser(Grant user) {
        this.user = user;
    }
}
