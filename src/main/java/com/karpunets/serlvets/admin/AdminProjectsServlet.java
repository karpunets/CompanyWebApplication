package com.karpunets.serlvets.admin;

import com.karpunets.dao.DAOFactory;
import com.karpunets.dao.GenericDAO;
import com.karpunets.listeners.ContextListener;
import com.karpunets.pojo.Project;
import com.karpunets.pojo.Qualification;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Set;

/**
 * @author Karpunets
 * @since 17.03.2017
 */
@WebServlet(name = "AdminProjectsServlet", urlPatterns = "/admin/projects")
public class AdminProjectsServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        SimpleDateFormat dateFormat = (SimpleDateFormat) request.getServletContext().getAttribute(ContextListener.DATE_FORMAT);
        request.setAttribute("dateFormat", dateFormat);
        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
        try (GenericDAO genericDAO = factory.getGenericDAO(Project.class)) {
            request.setAttribute("projectList", genericDAO.getAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
        request.getRequestDispatcher("/admin/projects.jsp").forward(request, response);

    }
}
