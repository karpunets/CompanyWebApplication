package com.karpunets.serlvets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Karpunets
 * @since 18.03.2017
 */
@WebServlet(name = "GrantsServlet", urlPatterns = "/grants")
public class GrantsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/grants.jsp").forward(request, response);
    }
}
