package com.karpunets.controllers;

import com.karpunets.dao.DAOFactory;
import com.karpunets.dao.GenericDAO;
import com.karpunets.jaas.UserPrincipal;
import com.karpunets.listeners.ContextListener;
import com.karpunets.pojo.grants.Grant;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;

/**
 * @author Karpunets
 * @since 14.03.2017
 */
@WebServlet(name = "UploadController", urlPatterns = "/upload/*")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 20)   // 50MB
public class UploadController extends HttpServlet {
    private static final String DIR = "\\cloud\\images\\avatars";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        switch (request.getPathInfo()) {
            case "/avatar":
                uploadAvatar(request, response);
                break;
        }
    }

    private void uploadAvatar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (request.authenticate(response)) {
            Grant grant = ((UserPrincipal) request.getUserPrincipal()).getUser();
            String appPath = request.getServletContext().getRealPath("");
            appPath = appPath.substring(0, appPath.length() - 5);

            File fileSaveDir = new File(appPath + DIR);
            if (!fileSaveDir.exists()) {
                fileSaveDir.mkdir();
            }

            for (Part part : request.getParts()) {
                String fileName = new File(grant.getId() + getType(part)).getName();
                part.write(fileSaveDir + File.separator + fileName);
                grant.setPhotoUrl(new File(DIR + File.separator + fileName));
                break;
            }

            DAOFactory factory = (DAOFactory) request.getServletContext().getAttribute(ContextListener.FACTORY_NAME);
            try (GenericDAO<Grant> genericDAO = factory.getGenericDAO(factory.getGeneralDAO().getClassByIdObject(grant.getId()))) {
                genericDAO.update(grant);
            } catch (Exception e) {
                e.printStackTrace();
            }

            response.sendRedirect("/profile");
        }

    }

    private String getType(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");

        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.lastIndexOf("."), s.length() - 1);
            }
        }
        return "";
    }

}
