package com.karpunets.serlvets.manager;

import com.karpunets.dao.DAOFactory;
import com.karpunets.dao.GenericDAO;
import com.karpunets.jaas.UserPrincipal;
import com.karpunets.listeners.ContextListener;
import com.karpunets.pojo.Project;
import com.karpunets.pojo.Qualification;
import com.karpunets.pojo.grants.Manager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * @author Karpunets
 * @since 14.03.2017
 */
@WebServlet(name = "EditProjectServlet", urlPatterns = "/manage/project_edit")
public class EditProjectServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (request.getParameter("id") == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        long idProject = Long.parseLong(request.getParameter("id"));

        SimpleDateFormat dateFormat = (SimpleDateFormat) request.getServletContext().getAttribute(ContextListener.DATE_FORMAT);

        Manager manager = (Manager) ((UserPrincipal) request.getUserPrincipal()).getUser();
        if (manager.getNewProjects() != null) {
            for (Project project : manager.getNewProjects()) {
                if (project.getId() == idProject) {
                    manager.carryOutProject(project);
                    DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
                    try (GenericDAO<Manager> genericDAO = factory.getGenericDAO(Manager.class)) {
                        genericDAO.update(manager);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (manager.getProjects() != null) {
            for (Project project : manager.getProjects()) {
                if (project.getId() == idProject) {
                    request.setAttribute("project", project);
                    request.setAttribute("qualifications", Qualification.values());
                    request.setAttribute("startingDate", dateFormat.format(project.getStartingDate()));
                    request.setAttribute("endingDate", dateFormat.format(project.getEndingDate()));
                    request.getRequestDispatcher("/manage/edit_project.jsp").forward(request, response);
                    return;
                }
            }
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

}
