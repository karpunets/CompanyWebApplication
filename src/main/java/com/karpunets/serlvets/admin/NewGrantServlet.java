package com.karpunets.serlvets.admin;

import com.karpunets.pojo.Qualification;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Karpunets
 * @since 17.03.2017
 */
@WebServlet(name = "NewGrantServlet", urlPatterns = "/admin/new_grant")
public class NewGrantServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("qualifications", Qualification.values());
        request.getRequestDispatcher("/admin/new_grant.jsp").forward(request, response);
    }
}
