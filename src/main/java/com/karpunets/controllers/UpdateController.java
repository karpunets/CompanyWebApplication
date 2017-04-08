package com.karpunets.controllers;


import com.google.gson.Gson;
import com.karpunets.dao.DAOFactory;
import com.karpunets.dao.GenericDAO;
import com.karpunets.jaas.UserPrincipal;
import com.karpunets.listeners.ContextListener;
import com.karpunets.pojo.Project;
import com.karpunets.pojo.Qualification;
import com.karpunets.pojo.Sprint;
import com.karpunets.pojo.Task;
import com.karpunets.pojo.grants.*;
import com.karpunets.validators.InputValidator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Karpunets
 * @since 13.03.2017
 */
@WebServlet(name = "UpdateController", urlPatterns = "/update/*")
public class UpdateController extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        switch (request.getPathInfo()) {
            case "/grant/information":
                updateGrantInformation(request, response);
                break;
            case "/grant/account":
                updateGrantAccount(request, response);
                break;
            case "/project":
                updateProject(request, response);
                break;
            case "/task":
                updateTask(request, response);
                break;
            case "/task/finish":
                updateTaskFinish(request, response);
                break;
            case "/project/finish":
                updateProjectFinish(request, response);
                break;
            case "/grant/project":
                updateGrantProject(request, response);
                break;
            case "/grant/task":
                updateGrantTask(request, response);
                break;
            case "/administrator/project":
                updateAdministratorProject(request, response);
                break;
        }
    }

    private void updateAdministratorProject(HttpServletRequest request, HttpServletResponse response) {
        Long projectId = Long.valueOf(request.getParameter("projectId"));
        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();
        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
        if (grant instanceof Administrator) {
            try (GenericDAO<Project> genericDAO = factory.getGenericDAO(Project.class)) {
                genericDAO.delete(genericDAO.get(projectId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateGrantTask(HttpServletRequest request, HttpServletResponse response) {

        Long taskId = Long.valueOf(request.getParameter("taskId"));
        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();
        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);

        try {
            if (grant instanceof Employee) {
                Employee employee = (Employee) grant;
                Task deletedTask = null;
                for (Task task : employee.getTasks()) {
                    if (task.getId().equals(taskId)) {
                        deletedTask = task;
                        break;
                    }
                }
                if (deletedTask != null) {
                    employee.removeTask(deletedTask);
                    try (GenericDAO<Employee> customerGenericDAO = factory.getGenericDAO(Employee.class)) {
                        customerGenericDAO.update(employee);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void updateGrantProject(HttpServletRequest request, HttpServletResponse response) {

        Long projectId = Long.valueOf(request.getParameter("projectId"));
        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();
        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);

        try {
            if (grant instanceof Customer) {
                Customer customer = (Customer) grant;
                for (Project project : customer.getProjects()) {
                    if (project.getId().equals(projectId)) {
                        customer.removeProject(project);
                        try (GenericDAO<Customer> customerGenericDAO = factory.getGenericDAO(Customer.class)) {
                            customerGenericDAO.update(customer);
                        }
                    }

                }
            } else if (grant instanceof Manager) {
                Manager manager = (Manager) grant;
                for (Project project : manager.getProjects()) {
                    if (project.getId().equals(projectId)) {
                        manager.removeProject(project);
                        try (GenericDAO<Manager> managerGenericDAO = factory.getGenericDAO(Manager.class)) {
                            managerGenericDAO.update(manager);
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateProjectFinish(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
        String projectId = request.getParameter("projectId");
        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();

        try (GenericDAO<Project> projectGenericDAO = factory.getGenericDAO(Project.class)) {
            Project project = projectGenericDAO.get(Long.parseLong(projectId));
            if (grant instanceof Manager) {
                Manager manager = (Manager) grant;
                if (manager.getProjects().contains(project)) {
                    Set<Long> tasksFinished = new LinkedHashSet<>();
                    for (Sprint sprint: project.getSprints()) {
                        for (Task task: sprint.getTasks()) {
                            if (!task.isFinished()) {
                                tasksFinished.add(task.getId());
                            }
                        }
                    }
                    if (tasksFinished.isEmpty()) {
                        project.finish();
                        projectGenericDAO.update(project);
                    } else {
                        response.getWriter().write(new Gson().toJson(tasksFinished));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateTaskFinish(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);

        String taskId = request.getParameter("taskId");

        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();
        try (GenericDAO<Task> taskGenericDAO = factory.getGenericDAO(Task.class)) {
            Task task = taskGenericDAO.get(Long.valueOf(taskId));
            if (grant instanceof Employee) {
                Employee employee = (Employee) grant;
                if (employee.getTasks().contains(task)) {
                    task.finish();
                    taskGenericDAO.update(task);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
        SimpleDateFormat dateFormat = (SimpleDateFormat) request.getServletContext().getAttribute(ContextListener.DATE_FORMAT);
        PrintWriter writer = response.getWriter();

        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();

        String taskId = request.getParameter("taskId");
        String description = request.getParameter("description").replace("\n", "<br/>");
        String estimate = request.getParameter("estimate");
        Date startingDate = null;

        try (GenericDAO<Task> taskGenericDAO = factory.getGenericDAO(Task.class)) {
            Task task = taskGenericDAO.get(Long.valueOf(taskId));
            if (grant instanceof Manager) {
                Manager manager = (Manager) grant;
                for (Project project : manager.getProjects()) {
                    for (Sprint sprint : project.getSprints()) {
                        if (sprint.getTasks().contains(task)) {

                            try {
                                startingDate = dateFormat.parse(request.getParameter("startingDate"));
                            } catch (ParseException e) {
//                              e.printStackTrace();
                            }

                            if (!(InputValidator.validateString(writer, description, "description") &
                                    InputValidator.validateNotNull(writer, startingDate, "Date of the starting") &
                                    InputValidator.validateNumber(writer, estimate, "Estimate"))) {
                                return;
                            }

                            task.setDescription(description);
                            task.setEstimate(Integer.valueOf(estimate));
                            task.setStartingDate(startingDate);

                            taskGenericDAO.update(task);
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }


    private void updateGrantInformation(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();

        String number = request.getParameter("number");
        String email = request.getParameter("email");
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");

        PrintWriter writer = response.getWriter();
        boolean update = true;

        if (InputValidator.validateNumber(writer, number) & InputValidator.validateEmail(writer, email)) {
            grant.setNumber(number);
            grant.setEmail(email);
        } else {
            update = false;
        }
        if (!oldPassword.isEmpty() || !newPassword.isEmpty()) {
            if (InputValidator.validatePassword(writer, oldPassword) && InputValidator.validatePassword(writer, newPassword)) {
                if (grant.getPassword().equals(oldPassword)) {
                    grant.setPassword(newPassword);
                } else {
                    writer.write("Old password isn't equals<br/>");
                    return;
                }
            } else {
                update = false;
            }
        }

        if (update) {
            DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
            try (GenericDAO<Grant> genericDAO = factory.getGenericDAO((Class<Grant>) grant.getClass())) {
                genericDAO.update(grant);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateGrantAccount(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.isUserInRole("administrator")) {
            String id = request.getParameter("id");
            String name = request.getParameter("name");
            String surname = request.getParameter("surname");
            String grantName = request.getParameter("grant");
            String qualification = request.getParameter("qualification");

            DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
            try (GenericDAO<Grant> genericDAO = factory.getGenericDAO(factory.getGeneralDAO().getClassByIdObject(Long.valueOf(id)))) {
                Grant grant = genericDAO.get(Long.valueOf(id));

                PrintWriter writer = response.getWriter();

                if (!qualification.isEmpty()) {
                    String grantClass = grant.getClass().getSimpleName().toLowerCase();
                    if (!grantClass.equals(grantName)) {
                        if (grant instanceof Manager) {
                            grant = factory.getGeneralDAO().managerToEmployee((Manager) grant);
                        } else if (grant instanceof Employee) {
                            grant = factory.getGeneralDAO().employeeToManager((Employee) grant);
                        }
//                    ((UserPrincipal) request.getUserPrincipal()).setUser(grant);
                    }
                    ((Employee) grant).setQualification(Qualification.valueOf(qualification));
                }

                if (InputValidator.validateName(writer, name) & InputValidator.validateSurname(writer, surname)) {
                    grant.setName(name);
                    grant.setSurname(surname);
                } else {
                    return;
                }

                genericDAO.update(grant);
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }
    }

    private void updateProject(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();
        String id = request.getParameter("id");
        String name = request.getParameter("name");
        String description = request.getParameter("description").replace("\n", "<br/>");

        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
        try (GenericDAO<Project> genericDAO = factory.getGenericDAO(Project.class)) {
            Project project = genericDAO.get(Long.valueOf(id));

            if (!(grant instanceof Manager) || !((Manager) grant).getProjects().contains(project)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            SimpleDateFormat dateFormat = (SimpleDateFormat) request.getServletContext().getAttribute(ContextListener.DATE_FORMAT);
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");

            Date startingDate = null;
            Date endingDate = null;
            try {
                startingDate = dateFormat.parse(request.getParameter("startingDate"));
                endingDate = dateFormat.parse(request.getParameter("endingDate"));
            } catch (ParseException e) {
//                e.printStackTrace();
            }
            if (InputValidator.validateString(response.getWriter(), name, "name") &
                    InputValidator.validateDates(response.getWriter(), startingDate, endingDate)) {
                project.setName(name);
                project.setDescription(description.isEmpty() ? null : description);
                project.setStartingDate(startingDate);
                project.setEndingDate(endingDate);

                genericDAO.update(project);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

}
