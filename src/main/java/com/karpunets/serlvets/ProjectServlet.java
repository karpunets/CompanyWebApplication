package com.karpunets.serlvets;

import com.karpunets.dao.DAOFactory;
import com.karpunets.dao.GenericDAO;
import com.karpunets.jaas.UserPrincipal;
import com.karpunets.listeners.ContextListener;
import com.karpunets.pojo.Project;
import com.karpunets.pojo.Sprint;
import com.karpunets.pojo.Task;
import com.karpunets.pojo.grants.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author Karpunets
 * @since 17.03.2017
 */
@WebServlet(name = "ProjectServlet", urlPatterns = "/project")
public class ProjectServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (request.getParameter("id") == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();
        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
        Project currentProject;
        try (GenericDAO<Project> genericDAO = factory.getGenericDAO(Project.class)) {
            currentProject = genericDAO.get(Long.parseLong(request.getParameter("id")));
            if (currentProject == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        boolean haveAccess = false;

        if (grant instanceof Manager) {
            Manager manager = (Manager) grant;
            if (manager.getNewProjects() != null) {
                for (Project project : manager.getNewProjects()) {
                    if (project == currentProject) {
                        System.out.println("carryOutProject");
                        manager.carryOutProject(project);
                        try (GenericDAO<Manager> genericDAO = factory.getGenericDAO(Manager.class)) {
                            genericDAO.update(manager);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
            if (manager.getProjects() != null) {
                for (Project project : manager.getProjects()) {
                    if (project == currentProject) {
                        haveAccess = true;
                        request.setAttribute("isProjectManager", true);
                    }
                }
            }

        }
        if (!haveAccess && grant instanceof Employee) {
            Employee employee = (Employee) grant;
            Set<Task> tasks = new HashSet<>();
            if (employee.getNewTasks() != null) {
                tasks.addAll(employee.getNewTasks());
            }
            if (employee.getTasks() != null) {
                tasks.addAll(employee.getTasks());
            }
            for (Sprint sprint : currentProject.getSprints()) {
                for (Task task : sprint.getTasks()) {
                    if (tasks.contains(task)) {
                        haveAccess = true;
                        break;
                    }
                }
            }
        } else if (grant instanceof Customer) {
            Customer customer = (Customer) grant;
            for (Project project : customer.getNewProjects()) {
                if (project == currentProject) {
                    customer.carryOutProject(project);
                    try (GenericDAO<Customer> genericDAO = factory.getGenericDAO(Customer.class)) {
                        genericDAO.update(customer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            for (Project project : customer.getProjects()) {
                if (project == currentProject) {
                    haveAccess = true;
                }
            }
        } else if (grant instanceof Administrator) {
            haveAccess = true;
        }

        if (haveAccess) {
            request.setAttribute("project", currentProject);
            long min = currentProject.getStartingDate().getTime();
            long max = currentProject.getEndingDate().getTime() - min;
            long current = new Date().getTime() - min;
            long progress = current * 100 / max;
            if (progress <= 0) {
                request.setAttribute("progress", false);
            } else {
                request.setAttribute("progress", progress);
            }
            request.getRequestDispatcher("/project.jsp").forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

}
