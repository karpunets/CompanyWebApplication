package com.karpunets.serlvets.admin;

import com.karpunets.dao.DAOFactory;
import com.karpunets.dao.GenericDAO;
import com.karpunets.listeners.ContextListener;
import com.karpunets.pojo.grants.Customer;
import com.karpunets.pojo.grants.Manager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * @author Karpunets
 * @since 17.03.2017
 */
@WebServlet(name = "NewProjectServlet", urlPatterns = "/admin/new_project")
public class NewProjectServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);

        LinkedHashMap<Long, String> managers = new LinkedHashMap<>();
        try (GenericDAO<Manager> genericDAO = factory.getGenericDAO(Manager.class)) {
            for (Manager manager : genericDAO.getAll()) {
                managers.put(manager.getId(), manager.getSurname() + " " + manager.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        request.setAttribute("managers", managers);

        LinkedHashMap<Long, String> customers = new LinkedHashMap<>();
        try (GenericDAO<Customer> genericDAO = factory.getGenericDAO(Customer.class)) {
            for (Customer customer : genericDAO.getAll()) {
                customers.put(customer.getId(), customer.getSurname() + " " + customer.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        request.setAttribute("customers", customers);

        request.getRequestDispatcher("/admin/new_project.jsp").forward(request, response);

    }

}
