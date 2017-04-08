package com.karpunets.serlvets;

import com.karpunets.jaas.UserPrincipal;
import com.karpunets.pojo.CompanyObject;
import com.karpunets.pojo.grants.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author Karpunets
 * @since 13.03.2017
 */
@WebServlet(name = "MainServlet", urlPatterns = {"/index.html"})
public class HomeServlet extends HttpServlet {


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserPrincipal userPrincipal = (UserPrincipal) request.getUserPrincipal();
        Grant grant = userPrincipal.getUser();

        LinkedHashMap<String, String> menuMap = new LinkedHashMap<>();
        LinkedHashMap<String, String> notices = new LinkedHashMap<>();

        menuMap.put("Profile", "/profile");
        menuMap.put("See all grants", "/grants");

        if (grant instanceof Administrator) {
            menuMap.put("Projects", "admin/projects");
            menuMap.put("New grant", "admin/new_grant");
            menuMap.put("New project", "admin/new_project");
        }

        if (grant instanceof Customer) {
            menuMap.put("Projects", "/projects");
            Customer customer = (Customer) grant;
            checkNotices(notices, customer.getNewProjects(), "New project", "/project");
        }

        if (grant instanceof Manager) {
            menuMap.put("Projects", "/projects");
            Manager manager = (Manager) grant;
            checkNotices(notices, manager.getNewProjects(), "New project", "/project");
        }

        if (grant instanceof Employee) {
            menuMap.put("Tasks", "/tasks");
            Employee employee = (Employee) grant;
            checkNotices(notices, employee.getNewTasks(), "New task", "/task");
        }

        if (!notices.isEmpty()) {
            request.setAttribute("notices", notices);
        }

        request.setAttribute("menuMap", menuMap);
        request.getRequestDispatcher("/home.jsp").forward(request, response);

    }

    private void checkNotices(LinkedHashMap<String, String> notices, Set<? extends CompanyObject> set, String begin, String page){
        if (set != null && !set.isEmpty()) {
            for (CompanyObject object: set) {
                notices.put(begin + " № " + object.getId(), page + "?id=" + object.getId());
            }
        }
    }
}
