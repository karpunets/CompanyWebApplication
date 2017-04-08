package com.karpunets.jaas;

import com.karpunets.dao.DAOFactory;
import com.karpunets.listeners.ContextListener;
import com.karpunets.pojo.grants.Grant;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Karpunets
 * @since 07.03.2017
 */

public class CompanyLoginModule implements LoginModule {

    private CallbackHandler handler;
    private Subject subject;
    private Grant user;
    private UserPrincipal userPrincipal;
    private RolePrincipal rolePrincipal;
//    private String login;
//    private List<String> userGroups;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String, ?> sharedState, Map<String, ?> options) {
        handler = callbackHandler;
        this.subject = subject;
    }

    @Override
    public boolean login() throws LoginException {

        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("login");
        callbacks[1] = new PasswordCallback("password", false);
        try {
            handler.handle(callbacks);
            String login = ((NameCallback) callbacks[0]).getName();
            String password = String.valueOf(((PasswordCallback) callbacks[1])
                    .getPassword());

            DAOFactory factory = ContextListener.getDaoFactory();
            user = factory.getGeneralDAO().searchGrant(login, password);
            if (user != null) {
                return true;
            }
            throw new LoginException("Authentication failed");
        } catch (IOException | UnsupportedCallbackException e) {
            throw new LoginException(e.getMessage());
        }

    }

    @Override
    public boolean commit() throws LoginException {

        userPrincipal = new UserPrincipal(user);
        subject.getPrincipals().add(userPrincipal);
        rolePrincipal = new RolePrincipal(user);
        subject.getPrincipals().add(rolePrincipal);

        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return false;
    }

    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(userPrincipal);
        subject.getPrincipals().remove(rolePrincipal);
        return true;
    }



}