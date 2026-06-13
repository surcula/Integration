package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.AbsenceBusiness;
import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.dto.EmployeeListDto;
import be.atc.erpprojetintegration_1.entities.Absence;
import be.atc.erpprojetintegration_1.enums.AbsenceStatus;
import be.atc.erpprojetintegration_1.enums.AbsenceType;
import be.atc.erpprojetintegration_1.tools.Result;
import org.primefaces.model.file.UploadedFile;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class AbsencesBean implements Serializable {
    private static final long serialVersionUID = 1L;
    @Inject private AbsenceBusiness absenceBusiness;
    @Inject private EmployeeBusiness employeeBusiness;
    @Inject private AuthBean authBean;

    private List<Absence> absences;
    private List<EmployeeListDto> employees;
    private Absence selectedAbsence;
    private Integer selectedEmployeeId;
    private UploadedFile document;
    private String reviewComment;

    @PostConstruct
    public void init() { load(); prepareNew(); }

    public void load() {
        Result<List<Absence>> result = authBean.isHrOrAdmin() ? absenceBusiness.getAllActive()
                : absenceBusiness.getActiveByEmployee(connectedId());
        absences = result.isSuccess() ? result.getData() : new ArrayList<Absence>();
        employees = new ArrayList<>();
        if (authBean.isHrOrAdmin()) {
            Result<List<EmployeeListDto>> employeeResult = employeeBusiness.getEmployeeList(false);
            if (employeeResult.isSuccess()) employees = employeeResult.getData();
        }
    }

    public void prepareNew() {
        selectedAbsence = new Absence();
        selectedAbsence.setStartDate(LocalDate.now());
        selectedAbsence.setEndDate(LocalDate.now());
        selectedAbsence.setAllDay(true);
        selectedAbsence.setStatus(AbsenceStatus.DRAFT);
        selectedEmployeeId = authBean.isHrOrAdmin() ? null : connectedId();
        document = null;
    }

    public void edit(Absence absence) {
        Result<Absence> result = absenceBusiness.getById(absence.getId());
        if (!result.isSuccess()) { message(FacesMessage.SEVERITY_ERROR, "Impossible de charger la demande."); return; }
        selectedAbsence = result.getData();
        selectedEmployeeId = selectedAbsence.getEmployee().getId();
        document = null;
    }

    public void saveDraft() { save(false); }
    public void submit() { save(true); }

    private void save(boolean submit) {
        if (!canEditSelected()) { message(FacesMessage.SEVERITY_ERROR, "Cette demande ne peut plus etre modifiee."); return; }
        if (selectedEmployeeId == null) { message(FacesMessage.SEVERITY_ERROR, "Selectionnez un employe."); return; }
        if (document != null && document.getFileName() != null && !document.getFileName().trim().isEmpty()) {
            if (!storeDocument()) return;
        }
        Result<Absence> result = absenceBusiness.saveRequest(selectedAbsence, selectedEmployeeId, submit);
        if (!result.isSuccess()) { message(FacesMessage.SEVERITY_ERROR, firstError(result)); return; }
        message(FacesMessage.SEVERITY_INFO, submit ? "Demande envoyee a la RH." : "Brouillon enregistre.");
        load(); prepareNew();
    }

    public void approve() { review(AbsenceStatus.APPROVED); }
    public void refuse() { review(AbsenceStatus.REFUSED); }
    private void review(AbsenceStatus status) {
        if (!authBean.isHrOrAdmin()) return;
        Result<Absence> result = absenceBusiness.review(selectedAbsence.getId(), connectedId(), status, reviewComment);
        if (!result.isSuccess()) { message(FacesMessage.SEVERITY_ERROR, firstError(result)); return; }
        message(FacesMessage.SEVERITY_INFO, status == AbsenceStatus.APPROVED ? "Absence approuvee." : "Absence refusee.");
        reviewComment = null; load();
    }

    public void cancel(Absence absence) {
        Result<Absence> result = absenceBusiness.cancel(absence.getId(), connectedId(), authBean.isHrOrAdmin());
        if (!result.isSuccess()) { message(FacesMessage.SEVERITY_ERROR, firstError(result)); return; }
        message(FacesMessage.SEVERITY_INFO, "Demande annulee."); load();
    }

    private boolean storeDocument() {
        try {
            String original = Paths.get(document.getFileName()).getFileName().toString();
            Path directory = Paths.get(System.getProperty("user.home"), "erp-rh", "absences");
            Files.createDirectories(directory);
            Path target = directory.resolve(UUID.randomUUID().toString() + "_" + original);
            try (InputStream input = document.getInputStream()) { Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING); }
            selectedAbsence.setDocumentName(original);
            selectedAbsence.setDocumentPath(target.toString());
            return true;
        } catch (Exception ex) {
            message(FacesMessage.SEVERITY_ERROR, "Impossible d'enregistrer le justificatif."); return false;
        }
    }

    public boolean canEdit(Absence absence) {
        return absence != null && absence.getStatus() == AbsenceStatus.DRAFT
                && absence.getEmployee().getId().equals(connectedId());
    }
    public boolean canEditSelected() { return selectedAbsence != null && (selectedAbsence.getId() == null || canEdit(selectedAbsence)); }
    public boolean canReviewSelected() { return authBean.isHrOrAdmin() && selectedAbsence != null && selectedAbsence.getStatus() == AbsenceStatus.PENDING; }
    public boolean getCanEditSelected() { return canEditSelected(); }
    public boolean getCanReviewSelected() { return canReviewSelected(); }
    public boolean canCancel(Absence absence) { return absence != null && absence.getStatus() != AbsenceStatus.CANCELLED && absence.getStatus() != AbsenceStatus.REFUSED; }
    public String statusClass(Absence absence) { return "absence-status absence-status-" + absence.getStatus().name().toLowerCase(); }
    public long getPendingCount() { return countByStatus(AbsenceStatus.PENDING); }
    public long getApprovedCount() { return countByStatus(AbsenceStatus.APPROVED); }
    public long getRefusedCount() { return countByStatus(AbsenceStatus.REFUSED); }

    private long countByStatus(AbsenceStatus status) {
        if (absences == null) {
            return 0;
        }
        return absences.stream()
                .filter(absence -> absence.getStatus() == status)
                .count();
    }

    public String getApprovedVacationCelebrationKey() {
        if (authBean.isHrOrAdmin() || absences == null) {
            return null;
        }

        String approvedVacationIds = absences.stream()
                .filter(absence -> absence.getType() == AbsenceType.ANNUAL_LEAVE)
                .filter(absence -> absence.getStatus() == AbsenceStatus.APPROVED)
                .filter(absence -> absence.getEndDate() != null && !absence.getEndDate().isBefore(LocalDate.now()))
                .map(absence -> String.valueOf(absence.getId()))
                .sorted()
                .collect(Collectors.joining("-"));

        return approvedVacationIds.isEmpty()
                ? null
                : "approved-vacation-" + connectedId() + "-" + approvedVacationIds;
    }
    private Integer connectedId() { return authBean.getConnectedEmployee() == null ? null : authBean.getConnectedEmployee().getId(); }
    private String firstError(Result<?> result) { return result.getErrors() == null || result.getErrors().isEmpty() ? "Operation impossible." : result.getErrors().values().iterator().next(); }
    private void message(FacesMessage.Severity severity, String text) { FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, text, null)); }

    public AbsenceType[] getTypes() { return AbsenceType.values(); }
    public AbsenceStatus[] getStatuses() { return AbsenceStatus.values(); }
    public List<String> getAbsenceEmployeeNames() {
        if (absences == null) {
            return new ArrayList<>();
        }
        return absences.stream()
                .filter(absence -> absence.getEmployee() != null)
                .map(absence -> (absence.getEmployee().getFirstName() + " "
                        + absence.getEmployee().getLastName()).trim())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    public List<Absence> getAbsences() { return absences; }
    public List<EmployeeListDto> getEmployees() { return employees; }
    public Absence getSelectedAbsence() { return selectedAbsence; }
    public void setSelectedAbsence(Absence selectedAbsence) { this.selectedAbsence = selectedAbsence; }
    public Integer getSelectedEmployeeId() { return selectedEmployeeId; }
    public void setSelectedEmployeeId(Integer selectedEmployeeId) { this.selectedEmployeeId = selectedEmployeeId; }
    public UploadedFile getDocument() { return document; }
    public void setDocument(UploadedFile document) { this.document = document; }
    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
}
