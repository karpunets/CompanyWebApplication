package com.karpunets.controllers;

import com.google.gson.Gson;
import com.karpunets.dao.DAOFactory;
import com.karpunets.dao.GenericDAO;
import com.karpunets.listeners.ContextListener;
import com.karpunets.pojo.Task;
import com.karpunets.pojo.dialog.Dialog;
import com.karpunets.pojo.dialog.Message;
import com.karpunets.pojo.grants.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;

/**
 * @author Karpunets
 * @since 13.03.2017
 */
@WebServlet(name = "GetController", urlPatterns = "/get/*")
public class GetController extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            switch (request.getPathInfo()) {
                case "/grants":
                    getGrants(request, response);
                    break;
                case "/employees":
                    getEmployees(request, response);
                    break;
                case "/tasks":
                    getTasks(request, response);
                    break;
                case "/messages":
                    getMessage(request, response);
                    break;
            }
    }

    private void getMessage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);

        String dialogId = request.getParameter("dialogId");
        int lengthMessage = Integer.parseInt(request.getParameter("lengthMessage"));

        try(GenericDAO<Dialog> dialogGenericDAO = factory.getGenericDAO(Dialog.class)) {
            Dialog dialog = dialogGenericDAO.get(Long.parseLong(dialogId));
            if (dialog.getMessages() != null && dialog.getMessages().size() > lengthMessage) {
                Message[] messages = dialog.getMessages().toArray(new Message[dialog.getMessages().size()]);
                System.out.println(lengthMessage);
                System.out.println(Arrays.toString(messages));
                System.out.println(Arrays.toString(Arrays.copyOfRange(messages, lengthMessage, messages.length)));

                Set<Map<String, String>> result = new LinkedHashSet<>();

                for (int i = lengthMessage; i < messages.length; i++) {
                    Map<String, String> map = new LinkedHashMap<>();
                    map.put("employeeId", String.valueOf(messages[i].getAuthor().getId()));
                    map.put("employeeName", messages[i].getAuthor().getName() + " " + messages[i].getAuthor().getSurname());
                    map.put("photoUrl", String.valueOf(messages[i].getAuthor().getPhotoUrl()));
                    map.put("text", messages[i].getText());
                    result.add(map);
                }

                Gson gson = new Gson();
                response.getWriter().write(gson.toJson(result));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getTasks(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
        try (GenericDAO<Task> genericDAO = factory.getGenericDAO(Task.class)) {
            Set<Task> tasks = genericDAO.getAll();

            Set<Long> result = new LinkedHashSet<>();

            for (Task task: tasks) {
                if (String.valueOf(task.getId()).contains(request.getParameter("query"))) {
                    result.add(task.getId());
                }
            }

            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getEmployees(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
        Set<Employee> employees = factory.getGeneralDAO().getEmployeesLike(request.getParameter("query"));

        Set<Map<String, String>> result = new LinkedHashSet<>();

        for (Employee employee: employees) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("id", String.valueOf(employee.getId()));
            map.put("name", employee.getName());
            map.put("surname", employee.getSurname());
            map.put("photoUrl", String.valueOf(employee.getPhotoUrl()));
            result.add(map);
        }

        Gson gson = new Gson();
        response.getWriter().write(gson.toJson(result));
    }

    private void getGrants(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String grantName = request.getParameter("grantName").trim();

        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
        Class grantClass;
        switch (grantName) {
            case "administrator":
                grantClass = Administrator.class;
                break;
            case "manager":
                grantClass = Manager.class;
                break;
            case "employee":
                grantClass = Employee.class;
                break;
            case "customer":
                grantClass = Customer.class;
                break;
            default:
                throw new InvalidParameterException();
        }
        try (GenericDAO genericDAO = factory.getGenericDAO(grantClass)) {
            Set grantList = genericDAO.getAll();
            request.setAttribute("grantList", grantList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.setContentType("text/plain");
        request.getRequestDispatcher("/ajax/grants.jsp").forward(request, response);

    }
}
