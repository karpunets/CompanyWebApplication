package com.karpunets.serlvets;

import com.karpunets.dao.DAOFactory;
import com.karpunets.dao.GenericDAO;
import com.karpunets.dao.utils.oracle.proxy.ProxyFactory;
import com.karpunets.jaas.UserPrincipal;
import com.karpunets.listeners.ContextListener;
import com.karpunets.pojo.Qualification;
import com.karpunets.pojo.grants.Administrator;
import com.karpunets.pojo.grants.Employee;
import com.karpunets.pojo.grants.Grant;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @author Karpunets
 * @since 14.03.2017
 */
@WebServlet(name = "ProfileServlet", urlPatterns = "/profile")
public class ProfileServlet extends HttpServlet {

//    public static final File DEFAULT_PHOTO = new File("\\cloud\\images\\avatars\\0.png");

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();
        Grant currentGrant = null;
        if (request.getParameter("id") == null) {
            currentGrant = grant;
        } else {
            long id = Long.parseLong(request.getParameter("id"));
            DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
            Class grantClass = factory.getGeneralDAO().getClassByIdObject(id);
            try (GenericDAO<Grant> genericDAO = factory.getGenericDAO(grantClass)) {
                currentGrant = genericDAO.get(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (currentGrant == null) {
            response.sendError(404);
            return;
        }
        boolean canEditInformation = false;
        if (grant.getId() == currentGrant.getId()) {
            canEditInformation = true;
        }
        boolean canEditAccountInformation = false;
        if (grant instanceof Administrator) {
            canEditAccountInformation = true;
            request.setAttribute("qualifications", Qualification.values());
        }
        if (currentGrant instanceof Employee) {
            request.setAttribute("qualification", ((Employee)currentGrant).getQualification());
        }
//        if (currentGrant.getPhotoUrl() == null) {
//            currentGrant.setPhotoUrl(DEFAULT_PHOTO);
//        }
        request.setAttribute("grant", currentGrant);
        if (currentGrant instanceof ProxyFactory.Proxy) {
            request.setAttribute("grantClass", currentGrant.getClass().getSuperclass().getSimpleName());
        } else {
            request.setAttribute("grantClass", currentGrant.getClass().getSimpleName());
        }
        request.setAttribute("canEditInformation", canEditInformation);
        request.setAttribute("canEditAccountInformation", canEditAccountInformation);
        request.getRequestDispatcher("/profile.jsp").forward(request, response);
    }
}
