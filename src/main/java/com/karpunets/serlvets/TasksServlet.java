package com.karpunets.serlvets;

import com.karpunets.jaas.UserPrincipal;
import com.karpunets.listeners.ContextListener;
import com.karpunets.pojo.Project;
import com.karpunets.pojo.Task;
import com.karpunets.pojo.grants.Employee;
import com.karpunets.pojo.grants.Grant;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Karpunets
 * @since 17.03.2017
 */
@WebServlet(name = "TasksServlet", urlPatterns = "/tasks")
public class TasksServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();

        if (grant instanceof Employee) {
            Employee employee = (Employee) grant;
            Set<Task> newTasks, tasks;
            newTasks = employee.getNewTasks();
            tasks = employee.getTasks();
            SimpleDateFormat dateFormat = (SimpleDateFormat) request.getServletContext().getAttribute(ContextListener.DATE_FORMAT);
            request.setAttribute("dateFormat", dateFormat);
            request.setAttribute("newTasks", newTasks == null ? new HashSet<Project>() : newTasks);
            request.setAttribute("tasks", tasks == null ? new HashSet<Project>() : tasks);
            request.getRequestDispatcher("/tasks.jsp").forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }

    }

}
