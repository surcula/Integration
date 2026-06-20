package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.AbsenceBusiness;
import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.dto.EmployeeListDto;
import be.atc.erpprojetintegration_1.entities.Absence;
import be.atc.erpprojetintegration_1.enums.AbsenceStatus;
import be.atc.erpprojetintegration_1.enums.AbsenceType;
import be.atc.erpprojetintegration_1.tools.Result;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStream;
import java.io.Serializable;
import java.io.IOException;
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
    private List<Absence> filteredAbsences;
    private List<AbsenceStatus> selectedStatuses = new ArrayList<>();
    private List<EmployeeListDto> employees;
    private Absence selectedAbsence;
    private Integer selectedEmployeeId;
    private UploadedFile document;
    private StreamedContent documentFile;
    private String reviewComment;
    private boolean departmentHead;

    @PostConstruct
    public void init() {
        Result<Boolean> headResult = absenceBusiness.isDepartmentHead(connectedId());
        departmentHead = headResult.isSuccess() && Boolean.TRUE.equals(headResult.getData());
        absenceBusiness.refuseOverdueSickness();
        load();
        prepareNew();
    }

    public void load() {
        Result<List<Absence>> result = absenceBusiness.getAccessible(connectedId(), authBean.isHrOrAdmin());
        absences = result.isSuccess() ? result.getData() : new ArrayList<Absence>();
        filterAbsences();
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
            selectedAbsence.setCertificateValidated(false);
            selectedAbsence.setCertificateValidatedBy(null);
            selectedAbsence.setCertificateValidatedAt(null);
        }
        Result<Absence> result = absenceBusiness.saveRequest(selectedAbsence, selectedEmployeeId, submit);
        if (!result.isSuccess()) { message(FacesMessage.SEVERITY_ERROR, firstError(result)); return; }
        message(FacesMessage.SEVERITY_INFO, submit ? "Demande envoyee pour validation." : "Brouillon enregistre.");
        load(); prepareNew();
    }

    public void approve() { review(AbsenceStatus.APPROVED); }
    public void refuse() { review(AbsenceStatus.REFUSED); }
    private void review(AbsenceStatus status) {
        if (!canReviewSelected()) return;
        Result<Absence> result = absenceBusiness.review(selectedAbsence.getId(), connectedId(), status,
                reviewComment, authBean.isHrOrAdmin());
        if (!result.isSuccess()) { message(FacesMessage.SEVERITY_ERROR, firstError(result)); return; }
        message(FacesMessage.SEVERITY_INFO, status == AbsenceStatus.APPROVED ? "Absence approuvee." : "Absence refusee.");
        reviewComment = null; load();
    }

    public void validateCertificate() {
        if (!authBean.isHrOrAdmin() || selectedAbsence == null) return;
        Result<Absence> result = absenceBusiness.validateCertificate(selectedAbsence.getId(), connectedId());
        if (!result.isSuccess()) { message(FacesMessage.SEVERITY_ERROR, firstError(result)); return; }
        selectedAbsence = result.getData();
        message(FacesMessage.SEVERITY_INFO, "Certificat valide par la RH.");
        load();
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
    public boolean canReviewSelected() {
        if (selectedAbsence == null || selectedAbsence.getStatus() != AbsenceStatus.PENDING) return false;
        if (selectedAbsence.getType() == AbsenceType.UNPAID_LEAVE) return authBean.isHrOrAdmin();
        return departmentHead || authBean.hasRole("ADMIN") || authBean.hasRole("HR");
    }
    public boolean canValidateCertificate() {
        return authBean.isHrOrAdmin() && selectedAbsence != null
                && selectedAbsence.getType() == AbsenceType.SICKNESS
                && selectedAbsence.getDocumentPath() != null
                && !Boolean.TRUE.equals(selectedAbsence.getCertificateValidated());
    }
    public boolean canApproveSelected() {
        return canReviewSelected() && (selectedAbsence.getType() != AbsenceType.SICKNESS
                || Boolean.TRUE.equals(selectedAbsence.getCertificateValidated()));
    }
    public boolean canAccessDocument() { return authBean.isHrOrAdmin(); }
    public void prepareDocument(Absence absence) {
        documentFile = null;
        if (!canAccessDocument()) {
            message(FacesMessage.SEVERITY_ERROR, "Acces refuse.");
            return;
        }
        if (absence == null || absence.getDocumentPath() == null || absence.getDocumentPath().trim().isEmpty()) {
            message(FacesMessage.SEVERITY_ERROR, "Aucun justificatif disponible.");
            return;
        }

        Path file = Paths.get(absence.getDocumentPath());
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            message(FacesMessage.SEVERITY_ERROR, "Le justificatif est introuvable sur le serveur.");
            return;
        }

        String fileName = absence.getDocumentName() == null || absence.getDocumentName().trim().isEmpty()
                ? file.getFileName().toString()
                : absence.getDocumentName();
        documentFile = DefaultStreamedContent.builder()
                .name(fileName)
                .contentType(resolveContentType(file))
                .stream(() -> {
                    try {
                        return Files.newInputStream(file);
                    } catch (IOException ex) {
                        throw new IllegalStateException("Impossible d'ouvrir le justificatif.", ex);
                    }
                })
                .build();
    }
    public boolean getCanAccessDocument() { return canAccessDocument(); }
    public boolean getCanValidateCertificate() { return canValidateCertificate(); }
    public boolean isDepartmentHead() { return departmentHead; }
    public boolean getCanEditSelected() { return canEditSelected(); }
    public boolean getCanReviewSelected() { return canReviewSelected(); }
    public boolean getCanApproveSelected() { return canApproveSelected(); }
    public boolean canCancel(Absence absence) {
        return absence != null && (authBean.isHrOrAdmin() || absence.getEmployee().getId().equals(connectedId()))
                && absence.getStatus() != AbsenceStatus.CANCELLED && absence.getStatus() != AbsenceStatus.REFUSED;
    }
    public String statusClass(Absence absence) { return "absence-status absence-status-" + absence.getStatus().name().toLowerCase(); }
    public long getPendingCount() { return countByStatus(AbsenceStatus.PENDING); }
    public long getApprovedCount() { return countByStatus(AbsenceStatus.APPROVED); }
    public long getRefusedCount() { return countByStatus(AbsenceStatus.REFUSED); }

    public void filterAbsences() {
        if (absences == null) {
            filteredAbsences = new ArrayList<>();
            return;
        }
        filteredAbsences = absences.stream()
                .filter(absence -> selectedStatuses.isEmpty() || selectedStatuses.contains(absence.getStatus()))
                .collect(Collectors.toList());
    }

    public void toggleStatusFilter(AbsenceStatus status) {
        if (selectedStatuses.contains(status)) {
            selectedStatuses.remove(status);
        } else {
            selectedStatuses.add(status);
        }
        filterAbsences();
    }

    public void clearStatusFilter() {
        selectedStatuses.clear();
        filterAbsences();
    }

    public boolean isAllStatusesSelected() { return selectedStatuses.isEmpty(); }
    public boolean isStatusSelected(AbsenceStatus status) { return selectedStatuses.contains(status); }
    public AbsenceStatus getPendingStatus() { return AbsenceStatus.PENDING; }
    public AbsenceStatus getApprovedStatus() { return AbsenceStatus.APPROVED; }
    public AbsenceStatus getRefusedStatus() { return AbsenceStatus.REFUSED; }

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
                .filter(absence -> absence.getEmployee() != null
                        && absence.getEmployee().getId().equals(connectedId()))
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
    private String resolveContentType(Path file) {
        try {
            String contentType = Files.probeContentType(file);
            return contentType == null ? "application/octet-stream" : contentType;
        } catch (IOException ex) {
            return "application/octet-stream";
        }
    }

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
    public List<Absence> getAbsences() { return filteredAbsences == null ? new ArrayList<Absence>() : filteredAbsences; }
    public List<EmployeeListDto> getEmployees() { return employees; }
    public Absence getSelectedAbsence() { return selectedAbsence; }
    public void setSelectedAbsence(Absence selectedAbsence) { this.selectedAbsence = selectedAbsence; }
    public StreamedContent getDocumentFile() { return documentFile; }
    public Integer getSelectedEmployeeId() { return selectedEmployeeId; }
    public void setSelectedEmployeeId(Integer selectedEmployeeId) { this.selectedEmployeeId = selectedEmployeeId; }
    public UploadedFile getDocument() { return document; }
    public void setDocument(UploadedFile document) { this.document = document; }
    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
}
