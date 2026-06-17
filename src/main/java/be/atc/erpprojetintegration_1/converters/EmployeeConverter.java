package be.atc.erpprojetintegration_1.converters;

import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.tools.EMF;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.persistence.EntityManager;

@FacesConverter("employeeConverter")
public class EmployeeConverter implements Converter<Employee> {

    @Override
    public Employee getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        EntityManager em = EMF.getEM();

        try {
            return em.find(Employee.class, Integer.valueOf(value));
        } finally {
            em.close();
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Employee employee) {
        if (employee == null || employee.getId() == null) {
            return "";
        }

        return employee.getId().toString();
    }
}
