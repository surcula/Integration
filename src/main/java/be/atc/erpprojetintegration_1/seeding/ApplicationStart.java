package be.atc.erpprojetintegration_1.seeding;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ApplicationStart implements ServletContextListener {

    @Inject
    private EmployeeStart employeeStart;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        employeeStart.createDefaultAdminIfNotExists();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
