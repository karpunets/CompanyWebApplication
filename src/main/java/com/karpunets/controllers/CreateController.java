package com.karpunets.controllers;

import com.google.gson.Gson;
import com.karpunets.dao.DAOFactory;
import com.karpunets.dao.GeneralDAO;
import com.karpunets.dao.GenericDAO;
import com.karpunets.jaas.UserPrincipal;
import com.karpunets.listeners.ContextListener;
import com.karpunets.pojo.Project;
import com.karpunets.pojo.Qualification;
import com.karpunets.pojo.Sprint;
import com.karpunets.pojo.Task;
import com.karpunets.pojo.dialog.Dialog;
import com.karpunets.pojo.dialog.Message;
import com.karpunets.pojo.grants.*;
import com.karpunets.validators.InputValidator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Karpunets
 * @since 21.03.2017
 */
@WebServlet(name = "CreateController", urlPatterns = "/create/*")
public class CreateController extends HttpServlet {

    private DAOFactory factory;
    public static final File DEFAULT_PHOTO = new File("/cloud/images/avatars/0.png");

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        switch (request.getPathInfo()) {
            case "/grant":
                createGrant(request, response);
                break;
            case "/project":
                createProject(request, response);
                break;
            case "/sprint":
                createSprint(request, response);
                break;
            case "/task":
                createTask(request, response);
                break;
            case "/message":
                createMessage(request, response);
                break;
        }
    }

    private void createMessage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);

        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();

        String taskId = request.getParameter("taskId");
        String text = request.getParameter("text");

        try (GenericDAO<Task> taskGenericDAO = factory.getGenericDAO(Task.class)) {
            Task task = taskGenericDAO.get(Long.parseLong(taskId));

            if (grant instanceof Employee) {
                Employee employee = (Employee) grant;
                if (employee.getTasks().contains(task)) {
                    Message message = new Message();
                    message.setAuthor(employee);
                    message.setText(text);
                    try (GenericDAO<Message> messageGenericDAO = factory.getGenericDAO(Message.class)) {
                        messageGenericDAO.insert(message);

                        Dialog dialog = task.getDialog();
                        dialog.addMessage(message);
                        try (GenericDAO<Dialog> dialogGenericDAO = factory.getGenericDAO(Dialog.class)) {
                            dialogGenericDAO.update(dialog);
                        }
                        return;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    private void createTask(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
        SimpleDateFormat dateFormat = (SimpleDateFormat) request.getServletContext().getAttribute(ContextListener.DATE_FORMAT);
        PrintWriter writer = response.getWriter();

        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();
        String projectId = request.getParameter("projectId");
        String parentId = request.getParameter("parentId");
        String parent = request.getParameter("parent");
        String description = request.getParameter("description").replace("\n", "<br/>");
        String estimate = request.getParameter("estimate");
        String qualification = request.getParameter("qualification");
        String employees = request.getParameter("employees");
        String subtasks = request.getParameter("subtasks");
        String dependencies = request.getParameter("dependencies");

        Date startingDate = null;

        try {
            startingDate = dateFormat.parse(request.getParameter("startingDate"));
        } catch (ParseException e) {
//                e.printStackTrace();
        }

        if (!(InputValidator.validateString(writer, description, "description") &
                InputValidator.validateNotNull(writer, startingDate, "Date of the starting") &
                InputValidator.validateNumber(writer, estimate, "Estimate") &
                InputValidator.validateString(writer, qualification, "qualification") &
                InputValidator.validateString(writer, employees, "employees"))) {
            return;
        }


        try (GenericDAO<Project> projectGenericDAO = factory.getGenericDAO(Project.class)) {
            Project project = projectGenericDAO.get(Long.valueOf(projectId));

            if (!(grant instanceof Manager) || !((Manager) grant).getProjects().contains(project)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            Task task = new Task();
            task.setDescription(description);
            task.setStartingDate(startingDate);
            task.setEstimate(Integer.valueOf(estimate));
            task.setQualification(Qualification.valueOf(qualification));
            Dialog dialog = new Dialog();
            try (GenericDAO<Dialog> dialogGenericDAO = factory.getGenericDAO(Dialog.class)) {
                dialogGenericDAO.insert(dialog);
            }
            task.setDialog(dialog);
            try (GenericDAO<Task> taskGenericDAO = factory.getGenericDAO(Task.class)) {

                if (!subtasks.isEmpty()) {
                    for (String subtask : subtasks.split(",")) {
                        task.addSubtasks(taskGenericDAO.get(Long.parseLong(subtask)));
                    }
                }

                if (!dependencies.isEmpty()) {
                    for (String dependency : dependencies.split(",")) {
                        task.addDependency(taskGenericDAO.get(Long.parseLong(dependency)));
                    }
                }

                if (!employees.isEmpty()) {
                    GeneralDAO generalDAO = factory.getGeneralDAO();

                    for (String employee : employees.split(",")) {
                        try (GenericDAO<Employee> employeeGenericDAO = factory.getGenericDAO(generalDAO.getClassByIdObject(Long.parseLong(employee)))) {
                            Employee emp = employeeGenericDAO.get(Long.parseLong(employee));
                            emp.addNewTask(task);
                            employeeGenericDAO.update(emp);
                        }
                    }
                }

                taskGenericDAO.insert(task);


                switch (parent) {
                    case "sprint":
                        try (GenericDAO<Sprint> sprintGenericDAO = factory.getGenericDAO(Sprint.class)) {
                            Sprint sprint = sprintGenericDAO.get(Long.parseLong(parentId));
                            sprint.addTask(task);
                            sprintGenericDAO.update(sprint);
                        }
                        break;
                    case "task":
                        break;
                }

                try (GenericDAO<Manager> managerGenericDAO = factory.getGenericDAO(Manager.class)) {
                    ((Manager) grant).addNewTask(task);
                    managerGenericDAO.update((Manager) grant);
                }

                writer.write(String.valueOf(task.getId()));
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    private void createGrant(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.isUserInRole("administrator")) {
            String login = request.getParameter("login");
            String password = request.getParameter("password");
            String name = request.getParameter("name");
            String surname = request.getParameter("surname");
            String grantName = request.getParameter("grant");
            String qualification = request.getParameter("qualification");

            factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");

            if (!isDataGrantCorrect(response, login, password, name, surname, grantName, qualification)) {
//                response.sendError(400);
                return;
            }

            Grant grant;
            switch (grantName) {
                case "administrator":
                    grant = new Administrator();
                    break;
                case "manager":
                    grant = new Manager();
                    break;
                case "employee":
                    grant = new Employee();
                    break;
                case "customer":
                    grant = new Customer();
                    break;
                default:
                    grant = null;
            }

            grant.setLogin(login);
            grant.setPassword(password);
            grant.setName(name);
            grant.setSurname(surname);
            grant.setPhotoUrl(DEFAULT_PHOTO);
            if (grant instanceof Employee) {
                ((Employee) grant).setQualification(Qualification.valueOf(qualification));
            }

            try (GenericDAO<Grant> genericDAO = factory.getGenericDAO(Grant.class)) {
                genericDAO.insert(grant);
            } catch (Exception e) {
                e.printStackTrace();
            }
            PrintWriter writer = response.getWriter();
            writer.write(String.valueOf(grant.getId()));

        }
    }

    private void createProject(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.isUserInRole("administrator")) {

            SimpleDateFormat dateFormat = (SimpleDateFormat) request.getServletContext().getAttribute(ContextListener.DATE_FORMAT);

            String name = request.getParameter("name");
            String description = request.getParameter("description").replace("\n", "<br/>");
            String managerId = request.getParameter("manager");
            String customerId = request.getParameter("customer");
            Date startingDate = null;
            Date endingDate = null;

            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");

            try {
                startingDate = dateFormat.parse(request.getParameter("startingDate"));
                endingDate = dateFormat.parse(request.getParameter("endingDate"));
            } catch (ParseException e) {
//                e.printStackTrace();
            }

            if (!isDataProjectCorrect(response, name, description, managerId, customerId, startingDate, endingDate)) {
//                response.sendError(400);
                return;
            }

            Project project = new Project();
            project.setName(name);
            project.setDescription(description.isEmpty() ? null : description);
            project.setStartingDate(startingDate);
            project.setEndingDate(endingDate);

            DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
            try (GenericDAO<Project> projectDAO = factory.getGenericDAO(Project.class)) {
                projectDAO.insert(project);
                try (GenericDAO<Manager> managerDAO = factory.getGenericDAO(Manager.class)) {
                    Manager manager = managerDAO.get(Long.parseLong(managerId));
                    manager.addNewProject(project);
                    managerDAO.update(manager);
                }
                try (GenericDAO<Customer> managerDAO = factory.getGenericDAO(Customer.class)) {
                    Customer customer = managerDAO.get(Long.parseLong(customerId));
                    customer.addNewProject(project);
                    managerDAO.update(customer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            response.getWriter().write(String.valueOf(project.getId()));
        }
    }

    private void createSprint(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);

        Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();
        String id = request.getParameter("id");
        String description = request.getParameter("description");

        try (GenericDAO<Project> genericDAO = factory.getGenericDAO(Project.class)) {
            Project project = genericDAO.get(Long.valueOf(id));

            if (!(grant instanceof Manager) || !((Manager) grant).getProjects().contains(project)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            Sprint sprint = new Sprint();
            sprint.setDescription(description.isEmpty() ? null : description);
            project.addSprint(sprint);

            try (GenericDAO<Sprint> sprintDAO = factory.getGenericDAO(Sprint.class)) {
                sprintDAO.insert(sprint);
                try (GenericDAO<Project> projectDAO = factory.getGenericDAO(Project.class)) {
                    projectDAO.update(project);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            response.getWriter().write(String.valueOf(sprint.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private boolean isDataGrantCorrect(HttpServletResponse response, String login, String password, String name,
                                       String surname, String grant, String qualification)
            throws ServletException, IOException {
        PrintWriter writer = response.getWriter();
        boolean result = InputValidator.validateLogin(writer, factory, login) &
                InputValidator.validatePassword(writer, password) &
                InputValidator.validateName(writer, name) &
                InputValidator.validateSurname(writer, surname);
        if (!InputValidator.validateString(writer, grant, "grant")) {
            result = false;
        } else {
            if ((grant.equals("manager") || grant.equals("employee")) &&
                    !InputValidator.validateString(writer, qualification, "qualification")) {
                result = false;
            }
        }
        return result;
    }

    private boolean isDataProjectCorrect(HttpServletResponse response, String name, String description,
                                         String managerId, String customerId, Date startingDate, Date endingDate)
            throws ServletException, IOException {
        PrintWriter writer = response.getWriter();
        return InputValidator.validateString(writer, name, "name") &
                InputValidator.validateString(writer, description, "description") &
                InputValidator.validateString(writer, managerId, "manager") &
                InputValidator.validateString(writer, customerId, "customer") &
                InputValidator.validateDates(writer, startingDate, endingDate);
    }

}
