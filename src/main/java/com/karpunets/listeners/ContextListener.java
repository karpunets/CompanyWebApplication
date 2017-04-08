package com.karpunets.listeners; /**
 * @author Karpunets
 * @since 17.03.2017
 */

import com.karpunets.dao.DAOFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.text.SimpleDateFormat;

@WebListener()
public class ContextListener implements ServletContextListener {

    public static final String FACTORY_NAME = "DAOFactory";
    public static final String DATE_FORMAT = "dateFormat";
    private static DAOFactory factory;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");;

    public void contextInitialized(ServletContextEvent sce) {
        factory = DAOFactory.getDAOFactory(DAOFactory.ORACLE);
        sce.getServletContext().setAttribute(FACTORY_NAME, factory);
        sce.getServletContext().setAttribute(DATE_FORMAT, dateFormat);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        try {
            ((DAOFactory) sce.getServletContext().getAttribute(FACTORY_NAME)).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DAOFactory getDaoFactory() {
        return factory;
    }

}
