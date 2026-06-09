package be.atc.erpprojetintegration_1.converters;

import be.atc.erpprojetintegration_1.entities.Authorization;
import be.atc.erpprojetintegration_1.tools.EMF;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.persistence.EntityManager;

@FacesConverter("authorizationConverter")
public class AuthorizationConverter implements Converter<Authorization> {

    @Override
    public Authorization getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        EntityManager em = EMF.getEM();

        try {
            return em.find(Authorization.class, Integer.valueOf(value));
        } finally {
            em.close();
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Authorization authorization) {
        if (authorization == null || authorization.getId() == null) {
            return "";
        }

        return authorization.getId().toString();
    }
}
