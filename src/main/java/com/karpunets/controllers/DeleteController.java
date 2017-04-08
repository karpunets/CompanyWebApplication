package com.karpunets.controllers;


import com.karpunets.dao.DAOFactory;
import com.karpunets.dao.GenericDAO;
import com.karpunets.jaas.UserPrincipal;
import com.karpunets.listeners.ContextListener;
import com.karpunets.pojo.Project;
import com.karpunets.pojo.Sprint;
import com.karpunets.pojo.Task;
import com.karpunets.pojo.grants.Grant;
import com.karpunets.pojo.grants.Manager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Karpunets
 * @since 13.03.2017
 */
@WebServlet(name = "DeleteController", urlPatterns = "/delete/*")
public class DeleteController extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        switch (request.getPathInfo()) {
            case "/sprint":
                deleteSprint(request, response);
                break;
            case "/task":
                deleteTask(request, response);
                break;
        }
    }

    private void deleteTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();
        String projectId = request.getParameter("projectId");
        String parentId = request.getParameter("parentId");
        String parentType = request.getParameter("parentType");
        String taskId = request.getParameter("taskId");

        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);

        try (GenericDAO<Project> projectGenericDAO = factory.getGenericDAO(Project.class)) {
            Project project = projectGenericDAO.get(Long.valueOf(projectId));

            if (!(grant instanceof Manager) || !((Manager) grant).getProjects().contains(project)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            try (GenericDAO<Task> taskGenericDAO = factory.getGenericDAO(Task.class)) {
                Task task = taskGenericDAO.get(Long.parseLong(taskId));
                switch (parentType) {
                    case ("sprint"):
                        for (Sprint sprint: project.getSprints()) {
                            if (sprint.getId() == Long.parseLong(parentId)) {
                                System.out.println(task.getId());
                                taskGenericDAO.delete(task);
                                System.out.println(sprint.getTasks());
                                sprint.removeTask(task);
                                System.out.println(sprint.getTasks());
                                GenericDAO<Sprint> sprintGenericDAO = factory.getGenericDAO(Sprint.class);
                                sprintGenericDAO.update(sprint);
                                break;
                            }
                        }
                        break;
                    case ("subtask"):
                        taskGenericDAO.delete(task);
                        break;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void deleteSprint(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();
        String projectId = request.getParameter("projectId");
        String sprintId = request.getParameter("sprintId");

        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);

        try (GenericDAO<Project> genericDAO = factory.getGenericDAO(Project.class)) {
            Project project = genericDAO.get(Long.valueOf(projectId));

            if (!(grant instanceof Manager) || !((Manager) grant).getProjects().contains(project)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            try (GenericDAO<Sprint> sprintDAO = factory.getGenericDAO(Sprint.class)) {
                for (Sprint sprint : project.getSprints()) {
                    if (sprint.getId() == Long.parseLong(sprintId)) {
                        project.getSprints().remove(sprint);
                        sprintDAO.delete(sprint);
                        break;
                    }
                }
                try (GenericDAO<Project> projectDAO = factory.getGenericDAO(Project.class)) {
                    projectDAO.update(project);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            response.getWriter().write(String.valueOf(true));
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

}
