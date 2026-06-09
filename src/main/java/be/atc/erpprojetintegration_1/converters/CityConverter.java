package be.atc.erpprojetintegration_1.converters;

import be.atc.erpprojetintegration_1.entities.City;
import be.atc.erpprojetintegration_1.tools.EMF;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.persistence.EntityManager;

@FacesConverter("cityConverter")
public class CityConverter implements Converter<City> {

    @Override
    public City getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        EntityManager em = EMF.getEM();

        try {
            return em.find(City.class, Integer.valueOf(value));
        } finally {
            em.close();
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, City city) {
        if (city == null || city.getId() == null) {
            return "";
        }

        return city.getId().toString();
    }
}
