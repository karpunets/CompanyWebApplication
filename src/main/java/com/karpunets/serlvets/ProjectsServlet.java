package com.karpunets.serlvets;

import com.karpunets.dao.DAOFactory;
import com.karpunets.dao.GenericDAO;
import com.karpunets.jaas.UserPrincipal;
import com.karpunets.listeners.ContextListener;
import com.karpunets.pojo.Project;
import com.karpunets.pojo.Sprint;
import com.karpunets.pojo.Task;
import com.karpunets.pojo.grants.Customer;
import com.karpunets.pojo.grants.Employee;
import com.karpunets.pojo.grants.Grant;
import com.karpunets.pojo.grants.Manager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Karpunets
 * @since 17.03.2017
 */
@WebServlet(name = "ProjectsServlet", urlPatterns = "/projects")
public class ProjectsServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Set<Project> newProjects, projects;
        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();

        if (grant instanceof Customer) {
            Customer customer = (Customer) grant;
            newProjects = customer.getNewProjects();
            projects = customer.getProjects();
        } else if (grant instanceof Manager) {
            Manager manager = (Manager) grant;
            newProjects = manager.getNewProjects();
            projects = manager.getProjects();
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        SimpleDateFormat dateFormat = (SimpleDateFormat) request.getServletContext().getAttribute(ContextListener.DATE_FORMAT);
        request.setAttribute("dateFormat", dateFormat);
        request.setAttribute("newProjects", newProjects == null ? new HashSet<Project>() : newProjects);
        request.setAttribute("projects", projects == null ? new HashSet<Project>() : projects);
        request.getRequestDispatcher("/projects.jsp").forward(request, response);
    }

}
