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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Karpunets
 * @since 17.03.2017
 */
@WebServlet(name = "TaskServlet", urlPatterns = "/task")
public class TaskServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (request.getParameter("id") == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();



        if (grant instanceof Employee) {
            Employee employee = (Employee) grant;
            DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
            try (GenericDAO<Task> genericDAO = factory.getGenericDAO(Task.class)) {
                Task currentTask = genericDAO.get(Long.parseLong(request.getParameter("id")));
                if (currentTask == null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                boolean canEdit = false;
                if (grant instanceof Manager) {
                    Manager manager = (Manager) grant;
                    for (Project project: manager.getProjects()) {
                        for (Sprint sprint: project.getSprints()) {
                            if (sprint.getTasks().contains(currentTask)) {
                                canEdit = true;
                                break;
                            }
                        }
                        if (canEdit) {
                            break;
                        }
                    }
                }

                if (employee.getNewTasks().contains(currentTask)) {
                    employee.carryOutTask(currentTask);
                    try (GenericDAO<Employee> employeeGenericDAO = factory.getGenericDAO(Employee.class)) {
                        employeeGenericDAO.update(employee);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (employee.getTasks().contains(currentTask)) {
                    System.out.println(currentTask.getDialog().getId());
                    request.setAttribute("task", currentTask);
                    request.setAttribute("canEdit", canEdit);
                    request.setAttribute("grant", grant);
                    request.setAttribute("grantPhotoUrl", grant.getPhotoUrl().toString().replace("\\", "\\\\"));
                    long max = currentTask.getEstimate() * 60 * 60 * 1000;
                    long current = new Date().getTime() - currentTask.getStartingDate().getTime();
                    long progress = current * 100 / max;
                    if (progress <= 0) {
                        request.setAttribute("progress", false);
                    } else {
                        request.setAttribute("progress", progress);
                    }
                    SimpleDateFormat dateFormat = (SimpleDateFormat) request.getServletContext().getAttribute(ContextListener.DATE_FORMAT);
                    request.setAttribute("startingDate", dateFormat.format(currentTask.getStartingDate()));
                    if (currentTask.getEndingDate() != null) {
                        request.setAttribute("endingDate", dateFormat.format(currentTask.getEndingDate()));
                    }
                    request.getRequestDispatcher("/task.jsp").forward(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }

            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }

    }

}
