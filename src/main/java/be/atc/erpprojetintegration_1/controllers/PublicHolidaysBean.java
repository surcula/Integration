package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.PublicHolidayBusiness;
import be.atc.erpprojetintegration_1.entities.PublicHoliday;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class PublicHolidaysBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PublicHolidayBusiness publicHolidayBusiness;

    @Inject
    private AuthBean authBean;

    private List<PublicHoliday> publicHolidays;
    private PublicHoliday selectedPublicHoliday;
    private Integer generationYear;

    @PostConstruct
    public void init() {
        generationYear = LocalDate.now().getYear();
        loadPublicHolidays();
        prepareNew();
    }

    public void prepareNew() {
        selectedPublicHoliday = new PublicHoliday();
        selectedPublicHoliday.setIsActive(true);
    }

    public void edit(PublicHoliday publicHoliday) {
        selectedPublicHoliday = publicHoliday;
    }

    public void save() {
        if (!authBean.isHrOrAdmin()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Vous n'etes pas autorise a modifier les jours feries.");
            return;
        }
        Result<PublicHoliday> result = publicHolidayBusiness.save(selectedPublicHoliday);
        if (!result.isSuccess()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Impossible d'enregistrer le jour ferie.");
            return;
        }
        addMessage(FacesMessage.SEVERITY_INFO, "Jour ferie enregistre.");
        loadPublicHolidays();
        prepareNew();
    }

    public void changeActiveStatus(PublicHoliday publicHoliday) {
        if (!authBean.isHrOrAdmin()) {
            return;
        }
        Result<Void> result = publicHolidayBusiness.setActive(publicHoliday.getId(), !publicHoliday.getIsActive());
        if (!result.isSuccess()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Impossible de modifier le jour ferie.");
            return;
        }
        loadPublicHolidays();
    }

    public void generateYear() {
        if (!authBean.isHrOrAdmin()) {
            return;
        }
        Result<Void> result = publicHolidayBusiness.generateBelgianHolidays(generationYear);
        if (!result.isSuccess()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Impossible de generer les jours feries pour cette annee.");
            return;
        }
        addMessage(FacesMessage.SEVERITY_INFO,
                "Les jours feries belges de " + generationYear + " sont disponibles.");
        loadPublicHolidays();
    }

    private void loadPublicHolidays() {
        Result<List<PublicHoliday>> result = publicHolidayBusiness.getAll();
        publicHolidays = result.isSuccess() ? result.getData() : new ArrayList<PublicHoliday>();
        if (!result.isSuccess()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Impossible de charger les jours feries.");
        }
    }

    private void addMessage(FacesMessage.Severity severity, String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, message, null));
    }

    public List<PublicHoliday> getPublicHolidays() {
        return publicHolidays;
    }

    public PublicHoliday getSelectedPublicHoliday() {
        return selectedPublicHoliday;
    }

    public void setSelectedPublicHoliday(PublicHoliday selectedPublicHoliday) {
        this.selectedPublicHoliday = selectedPublicHoliday;
    }

    public Integer getGenerationYear() {
        return generationYear;
    }

    public void setGenerationYear(Integer generationYear) {
        this.generationYear = generationYear;
    }
}
