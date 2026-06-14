package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.DepartmentBusiness;
import be.atc.erpprojetintegration_1.business.AbsenceBusiness;
import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.business.PlanningBusiness;
import be.atc.erpprojetintegration_1.business.PlanningEmployeeBusiness;
import be.atc.erpprojetintegration_1.business.PublicHolidayBusiness;
import be.atc.erpprojetintegration_1.dto.EmployeeListDto;
import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.entities.Absence;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.Planning;
import be.atc.erpprojetintegration_1.entities.PlanningEmployeeSwapRequest;
import be.atc.erpprojetintegration_1.entities.PlanningSwapProposal;
import be.atc.erpprojetintegration_1.entities.PlanningsEmployee;
import be.atc.erpprojetintegration_1.entities.PublicHoliday;
import be.atc.erpprojetintegration_1.enums.PlanningSwapStatus;
import be.atc.erpprojetintegration_1.enums.PlanningSwapProposalStatus;
import be.atc.erpprojetintegration_1.enums.PlanningStatus;
import be.atc.erpprojetintegration_1.tools.Result;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class PlanningBean implements Serializable {

    private static final Logger log = Logger.getLogger(PlanningBean.class);
    private static final long serialVersionUID = 1L;

    @Inject
    private PlanningBusiness planningBusiness;

    @Inject
    private PlanningEmployeeBusiness planningEmployeeBusiness;

    @Inject
    private DepartmentBusiness departmentBusiness;

    @Inject
    private EmployeeBusiness employeeBusiness;

    @Inject
    private AuthBean authBean;

    @Inject
    private PublicHolidayBusiness publicHolidayBusiness;

    @Inject
    private AbsenceBusiness absenceBusiness;

    private ScheduleModel eventModel;
    private ScheduleEvent<Integer> selectedEvent;
    private Planning selectedPlanning;
    private List<Department> departments;
    private List<EmployeeListDto> employees;
    private Integer selectedDepartmentId;
    private Integer planningDepartmentFilterId;
    private List<Integer> selectedEmployeeIds;
    private String startHourValue;
    private String endHourValue;
    private boolean allDay;
    private PlanningsEmployee selectedAssignment;
    private List<PlanningsEmployee> planningAssignments;
    private List<PlanningEmployeeSwapRequest> swapRequests;
    private List<PlanningSwapProposal> swapProposals;
    private List<PlanningSwapProposal> selectedSwapProposals;
    private PlanningEmployeeSwapRequest selectedSwapRequest;
    private String swapReason;
    private String swapReviewComment;
    private String[] swapReplacementEmployeeIds = new String[0];
    private boolean swapEmergencyMode;
    private String recurrenceMode = "NONE";
    private LocalDate recurrenceEndDate;
    private LocalDate calendarInitialDate;
    private boolean globalPlanningAccess;
    private List<Integer> managedDepartmentIds = new ArrayList<>();

    private int reportYear = LocalDate.now().getYear();
    private int reportMonth = LocalDate.now().getMonthValue();
    private Integer reportEmployeeId;
    private Integer reportDepartmentId;

    private String view = "timeGridWeek";
    private String locale = "fr";
    private String minTime = "00:00:00";
    private String maxTime = "24:00:00";
    private String slotDuration = "00:30:00";

    @PostConstruct
    public void init() {
        calendarInitialDate = LocalDate.now();
        globalPlanningAccess = authBean != null && authBean.isHrOrAdmin();
        if (!globalPlanningAccess) {
            Result<List<Integer>> accessResult = planningBusiness.getManagedDepartmentIds(getConnectedEmployeeId());
            managedDepartmentIds = accessResult.isSuccess() ? accessResult.getData() : new ArrayList<Integer>();
        }
        if (isCanManagePlanning()) {
            loadDepartments();
            loadEmployees();
            if (!globalPlanningAccess && !managedDepartmentIds.isEmpty()) {
                planningDepartmentFilterId = managedDepartmentIds.get(0);
                selectedDepartmentId = managedDepartmentIds.get(0);
                reportDepartmentId = managedDepartmentIds.get(0);
            }
        } else {
            departments = new ArrayList<>();
            employees = new ArrayList<>();
        }
        loadPlannings();
        loadSwapRequests();
        prepareNewPlanning(LocalDateTime.now().withMinute(0).withSecond(0).withNano(0));
    }

    public void loadPlannings() {
        eventModel = new DefaultScheduleModel();

        Result<List<Planning>> result = isCanManagePlanning()
                ? planningBusiness.getAccessibleActive(getConnectedEmployeeId(), globalPlanningAccess)
                : planningBusiness.getActiveByEmployee(getConnectedEmployeeId());
        if (!result.isSuccess()) {
            addErrorMessage("Impossible de charger le planning.");
            return;
        }

        for (Planning planning : result.getData()) {
            if (matchesDepartmentFilter(planning)) {
                eventModel.addEvent(toScheduleEvent(planning));
            }
        }

        loadPublicHolidays();
        loadApprovedAbsences();
    }

    private void loadApprovedAbsences() {
        Result<List<Absence>> result = isCanManagePlanning()
                ? absenceBusiness.getAllActive()
                : absenceBusiness.getActiveByEmployee(getConnectedEmployeeId());
        if (!result.isSuccess()) {
            log.warn("Unable to load approved absences in planning");
            return;
        }
        for (Absence absence : result.getData()) {
            if (isDepartmentHeadManager() && (absence.getEmployee() == null
                    || employees.stream().noneMatch(employee -> employee.getId().equals(absence.getEmployee().getId())))) {
                continue;
            }
            if (absence.getStatus() == be.atc.erpprojetintegration_1.enums.AbsenceStatus.APPROVED) {
                eventModel.addEvent(toScheduleEvent(absence));
            }
        }
    }

    private void loadPublicHolidays() {
        Result<List<PublicHoliday>> result = publicHolidayBusiness.getAll();
        if (!result.isSuccess()) {
            log.warn("Unable to load public holidays in planning");
            return;
        }

        for (PublicHoliday publicHoliday : result.getData()) {
            if (Boolean.TRUE.equals(publicHoliday.getIsActive())) {
                eventModel.addEvent(toScheduleEvent(publicHoliday));
            }
        }
    }

    private boolean matchesDepartmentFilter(Planning planning) {
        if (planningDepartmentFilterId == null) {
            return true;
        }

        return planning.getDepartment() != null
                && planningDepartmentFilterId.equals(planning.getDepartment().getId());
    }

    public void onDateSelect(SelectEvent<LocalDateTime> selectEvent) {
        if (!isCanManagePlanning()) {
            return;
        }
        calendarInitialDate = selectEvent.getObject().toLocalDate();
        prepareNewPlanning(selectEvent.getObject());
    }

    public void onEventSelect(SelectEvent<ScheduleEvent<Integer>> selectEvent) {
        selectedEvent = selectEvent.getObject();
        if ("PUBLIC_HOLIDAY".equals(selectedEvent.getGroupId()) || "ABSENCE".equals(selectedEvent.getGroupId())) {
            PrimeFaces.current().ajax().addCallbackParam("editablePlanningEvent", false);
            return;
        }

        PrimeFaces.current().ajax().addCallbackParam("editablePlanningEvent", true);
        selectedPlanning = loadPlanning(selectedEvent.getData());
        if (selectedPlanning == null) {
            return;
        }
        calendarInitialDate = selectedPlanning.getDate();
        selectedDepartmentId = selectedPlanning.getDepartment() != null ? selectedPlanning.getDepartment().getId() : null;
        loadSelectedAssignments(selectedPlanning.getId());
        loadSelectedAssignment();
        syncHourFields();
    }

    public void requestSwap() {
        if (selectedPlanning == null) {
            addErrorMessage("Cette action est reservee a l'employe affecte.");
            return;
        }
        Result<PlanningEmployeeSwapRequest> result = planningEmployeeBusiness.requestSwap(
                selectedPlanning.getId(), getConnectedEmployeeId(), swapReason, swapEmergencyMode);
        if (!result.isSuccess()) {
            addErrorMessage(swapErrorMessage(result, "Impossible d'envoyer la demande de swap."));
            return;
        }
        swapReason = null;
        swapEmergencyMode = false;
        loadSwapRequests();
        loadSelectedAssignment();
        addInfoMessage("Demande de swap envoyee au chef de departement.");
    }

    public void prepareSwapRequest() {
        swapReason = null;
        swapEmergencyMode = false;
    }

    public void selectSwapRequest(PlanningEmployeeSwapRequest request) {
        selectedSwapRequest = request;
        swapReplacementEmployeeIds = new String[0];
        swapReviewComment = request != null ? request.getReviewComment() : null;
        Result<List<PlanningSwapProposal>> proposals = request == null
                ? Result.ok(new ArrayList<PlanningSwapProposal>())
                : planningEmployeeBusiness.getProposalsForRequest(request.getId());
        selectedSwapProposals = proposals.isSuccess() ? proposals.getData() : new ArrayList<PlanningSwapProposal>();
    }

    public void proposeSwapReplacements() {
        if (!isCanManagePlanning() || selectedSwapRequest == null) {
            addErrorMessage("Vous n'etes pas autorise a traiter cette demande.");
            return;
        }
        List<Integer> replacementIds = new ArrayList<>();
        if (swapReplacementEmployeeIds != null) {
            for (String employeeId : swapReplacementEmployeeIds) {
                if (employeeId != null && !employeeId.trim().isEmpty()) {
                    try {
                        replacementIds.add(Integer.valueOf(employeeId));
                    } catch (NumberFormatException ex) {
                        log.warn("Invalid replacement employee id received: " + employeeId);
                    }
                }
            }
        }
        if (replacementIds.isEmpty()) {
            addErrorMessage("Selectionnez au moins un remplacant.");
            FacesContext.getCurrentInstance().validationFailed();
            return;
        }
        Result<Void> result = planningEmployeeBusiness.proposeReplacements(selectedSwapRequest.getId(),
                replacementIds, getConnectedEmployeeId(), swapReviewComment);
        if (!result.isSuccess()) {
            addErrorMessage(swapErrorMessage(result, "Impossible d'envoyer les propositions."));
            return;
        }
        addInfoMessage("Propositions envoyees aux remplacants.");
        loadSwapRequests();
        selectedSwapRequest = null;
    }

    public void acceptSwapProposal(Integer proposalId) {
        Result<Void> result = planningEmployeeBusiness.acceptProposal(proposalId, getConnectedEmployeeId());
        if (!result.isSuccess()) {
            addErrorMessage(swapErrorMessage(result, "Cette proposition ne peut plus etre acceptee."));
            return;
        }
        addInfoMessage("Echange accepte. Le planning a ete mis a jour.");
        loadSwapRequests();
        loadPlannings();
    }

    public void declineSwapProposal(Integer proposalId) {
        Result<Void> result = planningEmployeeBusiness.declineProposal(proposalId, getConnectedEmployeeId());
        if (!result.isSuccess()) {
            addErrorMessage("Impossible de refuser cette proposition.");
            return;
        }
        addInfoMessage("Proposition refusee.");
        loadSwapRequests();
    }

    public void refuseSwap() {
        if (!isCanManagePlanning() || selectedSwapRequest == null) {
            addErrorMessage("Vous n'etes pas autorise a traiter cette demande.");
            return;
        }
        Result<Void> result = planningEmployeeBusiness.refuse(selectedSwapRequest.getId(),
                getConnectedEmployeeId(), swapReviewComment);
        if (!result.isSuccess()) {
            addErrorMessage("Impossible de refuser la demande de swap.");
            return;
        }
        addInfoMessage("Demande de swap refusee.");
        loadSwapRequests();
        selectedSwapRequest = null;
    }

    public void cancelSwap(Integer requestId) {
        Result<Void> result = planningEmployeeBusiness.cancel(requestId, getConnectedEmployeeId());
        if (!result.isSuccess()) {
            addErrorMessage("Impossible d'annuler la demande de swap.");
            return;
        }
        addInfoMessage("Demande de swap annulee.");
        loadSwapRequests();
        loadSelectedAssignment();
    }

    public void onEventMove(ScheduleEntryMoveEvent moveEvent) {
        if (!isCanManagePlanning()) {
            addErrorMessage("Vous n'etes pas autorise a modifier le planning.");
            return;
        }
        ScheduleEvent<?> movedEvent = moveEvent.getScheduleEvent();
        if (movedEvent != null && movedEvent.getData() instanceof Integer) {
            Planning planning = loadPlanning((Integer) movedEvent.getData());
            if (planning == null) {
                return;
            }
            applyEventDates(planning, movedEvent);
            savePlanning(false);
        }
    }

    public void onEventResize(ScheduleEntryResizeEvent resizeEvent) {
        if (!isCanManagePlanning()) {
            addErrorMessage("Vous n'etes pas autorise a modifier le planning.");
            return;
        }
        ScheduleEvent<?> resizedEvent = resizeEvent.getScheduleEvent();
        if (resizedEvent != null && resizedEvent.getData() instanceof Integer) {
            Planning planning = loadPlanning((Integer) resizedEvent.getData());
            if (planning == null) {
                return;
            }
            applyEventDates(planning, resizedEvent);
            savePlanning(false);
        }
    }

    public void savePlanning() {
        if (!isCanManagePlanning()) {
            addErrorMessage("Vous n'etes pas autorise a modifier le planning.");
            return;
        }
        savePlanning(true);
    }

    public void duplicatePlanning() {
        if (!isCanManagePlanning() || selectedPlanning == null || selectedPlanning.getId() == null) {
            addErrorMessage("Selectionnez un evenement a dupliquer.");
            return;
        }
        Planning source = selectedPlanning;
        Planning copy = new Planning();
        copy.setDate(source.getDate() != null ? source.getDate().plusDays(1) : LocalDate.now());
        copy.setStartHour(source.getStartHour());
        copy.setEndHour(source.getEndHour());
        copy.setNote(source.getNote());
        copy.setType(source.getType());
        copy.setDescription(source.getDescription());
        copy.setDepartment(source.getDepartment());
        copy.setIsActive(true);
        copy.setStatus(PlanningStatus.DRAFT);

        Result<Planning> result = planningBusiness.save(copy, new ArrayList<>(selectedEmployeeIds),
                getConnectedEmployeeId(), globalPlanningAccess);
        if (!result.isSuccess()) {
            showPlanningSaveError(result);
            return;
        }

        selectedPlanning = result.getData();
        calendarInitialDate = selectedPlanning.getDate();
        loadPlannings();
        selectedDepartmentId = selectedPlanning.getDepartment() != null
                ? selectedPlanning.getDepartment().getId() : null;
        selectedEvent = null;
        recurrenceMode = "NONE";
        recurrenceEndDate = null;
        syncHourFields();
        addInfoMessage("Evenement duplique au "
                + selectedPlanning.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".");
    }

    public void onTypeChange() {
        if (!isTypeSupportsAllDay()) {
            allDay = false;
        }
    }

    public void deletePlanning() {
        if (!isCanManagePlanning()) {
            addErrorMessage("Vous n'etes pas autorise a supprimer cet element.");
            return;
        }
        if (selectedPlanning == null || selectedPlanning.getId() == null) {
            return;
        }

        Result<Void> result = planningBusiness.deactivate(selectedPlanning.getId(),
                getConnectedEmployeeId(), globalPlanningAccess);
        if (!result.isSuccess()) {
            addErrorMessage("Impossible de supprimer cet element du planning.");
            return;
        }

        calendarInitialDate = selectedPlanning.getDate();
        addInfoMessage("Element supprime du planning.");
        loadPlannings();
        prepareNewPlanning(calendarInitialDate.atStartOfDay());
    }

    public void publishPlanning() {
        if (!isCanManagePlanning() || selectedPlanning == null) {
            addErrorMessage("Selectionnez un planning a publier.");
            return;
        }

        if (!validateSelectedPlanning()) {
            return;
        }

        selectedPlanning.setDepartment(findSelectedDepartment());
        selectedPlanning.setIsActive(true);

        if (selectedPlanning.getId() == null && !"NONE".equals(recurrenceMode)) {
            List<LocalDate> recurrenceDates = buildRecurrenceDates();
            if (recurrenceDates == null) {
                return;
            }
            Result<List<Planning>> recurringResult = planningBusiness.saveRecurring(
                    selectedPlanning, selectedEmployeeIds, recurrenceDates,
                    getConnectedEmployeeId(), globalPlanningAccess);
            if (!recurringResult.isSuccess()) {
                showPlanningSaveError(recurringResult);
                return;
            }
            for (Planning planning : recurringResult.getData()) {
                Result<Planning> publicationResult = planningBusiness.publish(planning.getId(),
                        getConnectedEmployeeId(), globalPlanningAccess);
                if (!publicationResult.isSuccess()) {
                    showPlanningSaveError(publicationResult);
                    return;
                }
            }
            selectedPlanning = recurringResult.getData().get(0);
            selectedPlanning.setStatus(PlanningStatus.PUBLISHED);
            calendarInitialDate = selectedPlanning.getDate();
            recurrenceMode = "NONE";
            recurrenceEndDate = null;
            loadPlannings();
            addInfoMessage(recurringResult.getData().size() + " plannings publies.");
            return;
        }

        Result<Planning> saveResult = planningBusiness.save(selectedPlanning, selectedEmployeeIds,
                getConnectedEmployeeId(), globalPlanningAccess);
        if (!saveResult.isSuccess()) {
            showPlanningSaveError(saveResult);
            return;
        }
        selectedPlanning = saveResult.getData();
        Result<Planning> result = planningBusiness.publish(selectedPlanning.getId(),
                getConnectedEmployeeId(), globalPlanningAccess);
        if (!result.isSuccess()) {
            showPlanningSaveError(result);
            return;
        }
        selectedPlanning = result.getData();
        calendarInitialDate = selectedPlanning.getDate();
        loadPlannings();
        addInfoMessage("Planning publie. Il est visible par les employes affectes.");
    }

    public void cancelPlanning() {
        if (!isCanManagePlanning() || selectedPlanning == null || selectedPlanning.getId() == null) {
            addErrorMessage("Selectionnez un planning a annuler.");
            return;
        }
        calendarInitialDate = selectedPlanning.getDate();
        Result<Planning> result = planningBusiness.cancel(selectedPlanning.getId(),
                getConnectedEmployeeId(), globalPlanningAccess);
        if (!result.isSuccess()) {
            addErrorMessage("Impossible d'annuler ce planning.");
            return;
        }
        loadPlannings();
        prepareNewPlanning(calendarInitialDate.atStartOfDay());
        addInfoMessage("Planning annule. Il n'est plus visible par les employes.");
    }

    public void generateMonthlyReportByEmployee() throws IOException {
        if (!isCanManagePlanning()) return;
        if (reportEmployeeId == null) {
            addErrorMessage("Selectionnez un employe pour le rapport.");
            return;
        }
        Result<List<Planning>> result = planningBusiness.getByMonthAndEmployeeForManager(
                reportYear, reportMonth, reportEmployeeId, getConnectedEmployeeId(), globalPlanningAccess);
        if (!result.isSuccess()) {
            addErrorMessage("Impossible de charger les plannings.");
            return;
        }
        EmployeeListDto emp = employees.stream()
                .filter(e -> e.getId().equals(reportEmployeeId))
                .findFirst().orElse(null);
        String empName = emp != null ? emp.getFullName() : "Employe";
        String monthLabel = java.time.Month.of(reportMonth)
                .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH);
        String title = "Planning mensuel - " + empName + " - " + monthLabel + " " + reportYear;
        byte[] pdf = buildMonthlyPdf(title, result.getData(), true);
        streamPdf(pdf, "planning-mensuel-employe-" + reportMonth + "-" + reportYear + ".pdf");
    }

    public void generateMonthlyReportByDepartment() throws IOException {
        if (!isCanManagePlanning()) return;
        if (reportDepartmentId == null) {
            addErrorMessage("Selectionnez un departement pour le rapport.");
            return;
        }
        Result<List<Planning>> result = planningBusiness.getByMonthAndDepartmentForManager(
                reportYear, reportMonth, reportDepartmentId, getConnectedEmployeeId(), globalPlanningAccess);
        if (!result.isSuccess()) {
            addErrorMessage("Impossible de charger les plannings.");
            return;
        }
        Department dept = departments.stream()
                .filter(d -> d.getId().equals(reportDepartmentId))
                .findFirst().orElse(null);
        String deptName = dept != null ? dept.getDepartmentName() : "Departement";
        String monthLabel = java.time.Month.of(reportMonth)
                .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH);
        String title = "Planning mensuel - " + deptName + " - " + monthLabel + " " + reportYear;
        byte[] pdf = buildMonthlyPdf(title, result.getData(), false);
        streamPdf(pdf, "planning-mensuel-dept-" + reportMonth + "-" + reportYear + ".pdf");
    }

    private byte[] buildMonthlyPdf(String title, List<Planning> plannings) throws IOException {
        return buildMonthlyPdf(title, plannings, false);
    }

    private byte[] buildMonthlyPdf(String titleText, List<Planning> plannings, boolean showDepartment) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 42, 42, 42, 42);
        PdfWriter.getInstance(document, output);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

        Paragraph titlePara = new Paragraph(titleText, titleFont);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingAfter(20);
        document.add(titlePara);

        if (plannings.isEmpty()) {
            document.add(new Paragraph("Aucun planning pour cette periode.", bodyFont));
        } else {
            float[] cols = showDepartment
                    ? new float[]{1.5f, 1.2f, 1.2f, 1.5f, 2f}
                    : new float[]{1.5f, 1.2f, 1.2f, 2f, 1.5f};
            PdfPTable table = new PdfPTable(cols);
            table.setWidthPercentage(100);

            addHeaderCell(table, "Date", headingFont);
            addHeaderCell(table, "Debut", headingFont);
            addHeaderCell(table, "Fin", headingFont);
            addHeaderCell(table, "Titre", headingFont);
            addHeaderCell(table, showDepartment ? "Service" : "Type", headingFont);

            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (Planning p : plannings) {
                addBodyCell(table, p.getDate() != null ? p.getDate().format(dateFmt) : "-", bodyFont);
                addBodyCell(table, p.getStartHour() != null ? p.getStartHour().toString() : "Journee", bodyFont);
                addBodyCell(table, p.getEndHour() != null ? p.getEndHour().toString() : "-", bodyFont);
                addBodyCell(table, p.getNote() != null ? p.getNote() : "-", bodyFont);
                if (showDepartment) {
                    addBodyCell(table, p.getDepartment() != null ? p.getDepartment().getDepartmentName() : "-", bodyFont);
                } else {
                    addBodyCell(table, p.getType() != null ? p.getType() : "-", bodyFont);
                }
            }
            document.add(table);
        }

        document.close();
        return output.toByteArray();
    }

    private void streamPdf(byte[] pdf, String fileName) throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        javax.servlet.http.HttpServletResponse response =
                (javax.servlet.http.HttpServletResponse) context.getExternalContext().getResponse();
        response.reset();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
        response.getOutputStream().flush();
        context.responseComplete();
    }

    public void generateReport() throws IOException {
        if (!isCanManagePlanning() || selectedPlanning == null || selectedPlanning.getId() == null) {
            addErrorMessage("Selectionnez un planning pour generer le rapport.");
            return;
        }
        Result<List<Employee>> employeesResult = planningBusiness.getAssignedEmployees(selectedPlanning.getId());
        if (!employeesResult.isSuccess()) {
            addErrorMessage("Impossible de charger les employes du rapport.");
            return;
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 42, 42, 42, 42);
        PdfWriter.getInstance(document, output);
        document.open();
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph title = new Paragraph("Rapport de planning", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(18);
        document.add(title);

        PdfPTable details = new PdfPTable(new float[]{1.2f, 2.8f});
        details.setWidthPercentage(100);
        addReportRow(details, "Titre", selectedPlanning.getNote(), headingFont, bodyFont);
        addReportRow(details, "Statut", planningStatusLabel(selectedPlanning), headingFont, bodyFont);
        addReportRow(details, "Date", selectedPlanning.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), headingFont, bodyFont);
        addReportRow(details, "Horaire", formatPlanningHours(selectedPlanning), headingFont, bodyFont);
        addReportRow(details, "Duree", calculatePlanningHours(selectedPlanning), headingFont, bodyFont);
        addReportRow(details, "Departement", selectedPlanning.getDepartment() == null
                ? "Aucun" : selectedPlanning.getDepartment().getDepartmentName(), headingFont, bodyFont);
        addReportRow(details, "Type", selectedPlanning.getType(), headingFont, bodyFont);
        addReportRow(details, "Description", selectedPlanning.getDescription(), headingFont, bodyFont);
        document.add(details);

        Paragraph employeeTitle = new Paragraph("Employes affectes", headingFont);
        employeeTitle.setSpacingBefore(18);
        employeeTitle.setSpacingAfter(8);
        document.add(employeeTitle);
        PdfPTable employeeTable = new PdfPTable(new float[]{2.2f, 2.8f});
        employeeTable.setWidthPercentage(100);
        addHeaderCell(employeeTable, "Employe", headingFont);
        addHeaderCell(employeeTable, "E-mail", headingFont);
        if (employeesResult.getData().isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("Aucun employe affecte", bodyFont));
            empty.setColspan(2);
            empty.setPadding(7);
            employeeTable.addCell(empty);
        } else {
            for (Employee employee : employeesResult.getData()) {
                addBodyCell(employeeTable, employee.getFirstName() + " " + employee.getLastName(), bodyFont);
                addBodyCell(employeeTable, employee.getEmail(), bodyFont);
            }
        }
        document.add(employeeTable);
        document.close();

        FacesContext context = FacesContext.getCurrentInstance();
        javax.servlet.http.HttpServletResponse response =
                (javax.servlet.http.HttpServletResponse) context.getExternalContext().getResponse();
        String fileName = "planning-" + selectedPlanning.getDate() + "-" + selectedPlanning.getId() + ".pdf";
        response.reset();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setContentLength(output.size());
        response.getOutputStream().write(output.toByteArray());
        response.getOutputStream().flush();
        context.responseComplete();
    }

    private void addReportRow(PdfPTable table, String label, String value, Font heading, Font body) {
        addHeaderCell(table, label, heading);
        addBodyCell(table, value == null || value.trim().isEmpty() ? "-" : value, body);
    }

    private void addHeaderCell(PdfPTable table, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setPadding(7);
        cell.setBackgroundColor(new java.awt.Color(226, 232, 240));
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value == null ? "-" : value, font));
        cell.setPadding(7);
        table.addCell(cell);
    }

    private String formatPlanningHours(Planning planning) {
        if (planning.getStartHour() == null || planning.getEndHour() == null) return "Toute la journee";
        String suffix = planning.getEndHour().isBefore(planning.getStartHour()) ? " (lendemain)" : "";
        return planning.getStartHour() + " - " + planning.getEndHour() + suffix;
    }

    private String calculatePlanningHours(Planning planning) {
        PlanningsEmployee assignment = new PlanningsEmployee();
        assignment.setPlanning(planning);
        return assignment.calculateHours().toPlainString() + " h";
    }

    private void savePlanning(boolean showMessage) {
        if (!validateSelectedPlanning()) {
            return;
        }

        selectedPlanning.setDepartment(findSelectedDepartment());
        selectedPlanning.setIsActive(true);

        if (selectedPlanning.getId() == null && !"NONE".equals(recurrenceMode)) {
            List<LocalDate> recurrenceDates = buildRecurrenceDates();
            if (recurrenceDates == null) {
                return;
            }
            Result<List<Planning>> recurringResult = planningBusiness.saveRecurring(
                    selectedPlanning, selectedEmployeeIds, recurrenceDates,
                    getConnectedEmployeeId(), globalPlanningAccess);
            if (!recurringResult.isSuccess()) {
                showPlanningSaveError(recurringResult);
                return;
            }
            if (showMessage) {
                addInfoMessage(recurringResult.getData().size() + " evenements crees.");
            }
            selectedPlanning = recurringResult.getData().get(0);
            calendarInitialDate = selectedPlanning.getDate();
            loadPlannings();
            selectedDepartmentId = selectedPlanning.getDepartment() != null
                    ? selectedPlanning.getDepartment().getId() : null;
            recurrenceMode = "NONE";
            recurrenceEndDate = null;
            return;
        }

        Result<Planning> result = planningBusiness.save(selectedPlanning, selectedEmployeeIds,
                getConnectedEmployeeId(), globalPlanningAccess);
        if (!result.isSuccess()) {
            showPlanningSaveError(result);
            return;
        }

        if (showMessage) {
            addInfoMessage("Planning enregistre.");
        }

        selectedPlanning = result.getData();
        calendarInitialDate = selectedPlanning.getDate();
        loadPlannings();
        selectedDepartmentId = selectedPlanning.getDepartment() != null ? selectedPlanning.getDepartment().getId() : null;
    }

    private void showPlanningSaveError(Result<?> result) {
        if (result.getErrors() != null && result.getErrors().containsKey("conflicts")) {
            String date = result.getErrors().get("recurrenceDate");
            String suffix = date == null ? "" : " le " + LocalDate.parse(date)
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            addErrorMessage("Conflit de planning" + suffix + " pour : "
                    + result.getErrors().get("conflicts") + ".");
            return;
        }
        addErrorMessage("Impossible d'enregistrer le planning.");
    }

    private List<LocalDate> buildRecurrenceDates() {
        if (recurrenceEndDate == null || recurrenceEndDate.isBefore(selectedPlanning.getDate())) {
            addErrorMessage("La date de fin de recurrence doit suivre la premiere date.");
            return null;
        }
        int step = "WEEKLY".equals(recurrenceMode) ? 7 : 1;
        List<LocalDate> dates = new ArrayList<>();
        LocalDate date = selectedPlanning.getDate();
        while (!date.isAfter(recurrenceEndDate)) {
            dates.add(date);
            if (dates.size() > 366) {
                addErrorMessage("La recurrence est limitee a 366 evenements.");
                return null;
            }
            date = date.plusDays(step);
        }
        return dates;
    }

    private boolean validateSelectedPlanning() {
        if (selectedPlanning == null) {
            addErrorMessage("Aucun element de planning selectionne.");
            return false;
        }

        try {
            syncPlanningHours();
        } catch (Exception ex) {
            addErrorMessage("Les heures doivent respecter le format HH:mm.");
            return false;
        }

        if (selectedPlanning.getDate() == null) {
            addErrorMessage("La date est obligatoire.");
            return false;
        }

        if (selectedPlanning.getNote() == null || selectedPlanning.getNote().trim().isEmpty()) {
            addErrorMessage("Le titre est obligatoire.");
            return false;
        }

        if (!allDay && (selectedPlanning.getStartHour() == null || selectedPlanning.getEndHour() == null)) {
            addErrorMessage("Les heures de debut et de fin sont obligatoires.");
            return false;
        }

        if (selectedPlanning.getStartHour() != null
                && selectedPlanning.getEndHour() != null
                && selectedPlanning.getEndHour().equals(selectedPlanning.getStartHour())) {
            addErrorMessage("Les heures de debut et de fin doivent etre differentes.");
            return false;
        }

        selectedPlanning.setNote(selectedPlanning.getNote().trim());
        selectedPlanning.setType(trim(selectedPlanning.getType()));
        selectedPlanning.setDescription(trim(selectedPlanning.getDescription()));
        return true;
    }

    private void prepareNewPlanning(LocalDateTime startDate) {
        selectedPlanning = new Planning();
        selectedPlanning.setDate(startDate.toLocalDate());
        selectedPlanning.setStartHour(startDate.toLocalTime());
        selectedPlanning.setEndHour(startDate.plusHours(1).toLocalTime());
        selectedPlanning.setType("SERVICE");
        selectedPlanning.setIsActive(true);
        selectedPlanning.setStatus(PlanningStatus.DRAFT);
        allDay = false;
        selectedDepartmentId = planningDepartmentFilterId;
        selectedEmployeeIds = new ArrayList<>();
        recurrenceMode = "NONE";
        recurrenceEndDate = null;
        selectedEvent = null;
        selectedAssignment = null;
        planningAssignments = new ArrayList<>();
        syncHourFields();
    }

    private ScheduleEvent<Integer> toScheduleEvent(Planning planning) {
        LocalDateTime startDate = toStartDate(planning);
        LocalDateTime endDate = toEndDate(planning, startDate);

        return DefaultScheduleEvent.<Integer>builder()
                .id(String.valueOf(planning.getId()))
                .title(buildTitle(planning))
                .startDate(startDate)
                .endDate(endDate)
                .description(planning.getDescription())
                .data(planning.getId())
                .allDay(planning.getStartHour() == null)
                .borderColor(resolveColor(planning.getType()))
                .backgroundColor(resolveColor(planning.getType()))
                .textColor(resolveTextColor(planning.getType()))
                .build();
    }

    private ScheduleEvent<Integer> toScheduleEvent(PublicHoliday publicHoliday) {
        LocalDateTime startDate = publicHoliday.getHolidayDate().atStartOfDay();
        return DefaultScheduleEvent.<Integer>builder()
                .id("holiday-" + publicHoliday.getId())
                .groupId("PUBLIC_HOLIDAY")
                .title(publicHoliday.getName())
                .startDate(startDate)
                .endDate(startDate.plusDays(1))
                .description(publicHoliday.getDescription())
                .allDay(true)
                .editable(false)
                .draggable(false)
                .resizable(false)
                .borderColor("#ef4444")
                .backgroundColor("#ef4444")
                .textColor("#ffffff")
                .build();
    }

    private ScheduleEvent<Integer> toScheduleEvent(Absence absence) {
        boolean isAllDay = Boolean.TRUE.equals(absence.getAllDay());
        LocalDateTime startDate = isAllDay ? absence.getStartDate().atStartOfDay()
                : absence.getStartDate().atTime(absence.getStartHour());
        LocalDateTime endDate = isAllDay ? absence.getEndDate().plusDays(1).atStartOfDay()
                : absence.getEndDate().atTime(absence.getEndHour());
        String employeeName = absence.getEmployee().getFirstName() + " " + absence.getEmployee().getLastName();
        return DefaultScheduleEvent.<Integer>builder()
                .id("absence-" + absence.getId())
                .groupId("ABSENCE")
                .title(absence.getType().getLabel() + " - " + employeeName)
                .startDate(startDate)
                .endDate(endDate)
                .description(absence.getComment())
                .allDay(isAllDay)
                .editable(false)
                .borderColor("#7c3aed")
                .backgroundColor("#7c3aed")
                .textColor("#ffffff")
                .build();
    }

    private Planning loadPlanning(Integer planningId) {
        Result<Planning> result = isCanManagePlanning()
                ? planningBusiness.getByIdForManager(planningId, getConnectedEmployeeId(), globalPlanningAccess)
                : planningBusiness.getByIdForEmployee(planningId, getConnectedEmployeeId());
        if (!result.isSuccess()) {
            addErrorMessage("Impossible de charger cet element du planning.");
            return null;
        }
        return result.getData();
    }

    private Integer getConnectedEmployeeId() {
        return authBean.getConnectedEmployee() != null
                ? authBean.getConnectedEmployee().getId()
                : null;
    }

    private String buildTitle(Planning planning) {
        StringBuilder title = new StringBuilder();
        if (planning.getStatus() == null || planning.getStatus() == PlanningStatus.DRAFT) {
            title.append("[Brouillon] ");
        }
        title.append(planning.getNote() != null ? planning.getNote() : "Planning");

        if (planning.getDepartment() != null && planning.getDepartment().getDepartmentName() != null) {
            title.append(" - ").append(planning.getDepartment().getDepartmentName());
        }

        return title.toString();
    }

    private LocalDateTime toStartDate(Planning planning) {
        LocalDate date = planning.getDate();
        LocalTime startHour = planning.getStartHour() != null ? planning.getStartHour() : LocalTime.MIN;
        return LocalDateTime.of(date, startHour);
    }

    private LocalDateTime toEndDate(Planning planning, LocalDateTime startDate) {
        if (planning.getEndHour() == null) {
            return planning.getStartHour() == null ? startDate.plusDays(1) : startDate.plusHours(1);
        }

        LocalDate endDate = planning.getEndHour().isBefore(planning.getStartHour())
                ? planning.getDate().plusDays(1) : planning.getDate();
        return LocalDateTime.of(endDate, planning.getEndHour());
    }

    private void applyEventDates(Planning planning, ScheduleEvent<?> scheduleEvent) {
        calendarInitialDate = scheduleEvent.getStartDate().toLocalDate();
        planning.setDate(scheduleEvent.getStartDate().toLocalDate());
        planning.setStartHour(scheduleEvent.isAllDay() ? null : scheduleEvent.getStartDate().toLocalTime());
        planning.setEndHour(scheduleEvent.isAllDay() ? null : scheduleEvent.getEndDate().toLocalTime());
        allDay = scheduleEvent.isAllDay() && supportsAllDay(planning.getType());
        selectedPlanning = planning;
        selectedDepartmentId = planning.getDepartment() != null ? planning.getDepartment().getId() : null;
        loadSelectedAssignments(planning.getId());
        syncHourFields();
    }

    private void syncHourFields() {
        allDay = selectedPlanning != null
                && selectedPlanning.getStartHour() == null
                && selectedPlanning.getEndHour() == null
                && supportsAllDay(selectedPlanning.getType());
        startHourValue = selectedPlanning != null && selectedPlanning.getStartHour() != null
                ? selectedPlanning.getStartHour().toString()
                : "";
        endHourValue = selectedPlanning != null && selectedPlanning.getEndHour() != null
                ? selectedPlanning.getEndHour().toString()
                : "";
    }

    private void syncPlanningHours() {
        if (allDay && isTypeSupportsAllDay()) {
            selectedPlanning.setStartHour(null);
            selectedPlanning.setEndHour(null);
            return;
        }

        selectedPlanning.setStartHour(parseHour(startHourValue));
        selectedPlanning.setEndHour(parseHour(endHourValue));
    }

    private boolean supportsAllDay(String type) {
        if (type == null) {
            return false;
        }

        switch (type.trim().toUpperCase()) {
            case "REPOS":
            case "RECUP":
            case "RECUPERATION":
            case "FERIE":
            case "HOLIDAY":
            case "VACANCES":
            case "CONGE":
                return true;
            default:
                return false;
        }
    }

    private LocalTime parseHour(String value) {
        if (value == null || value.trim().isEmpty() || value.contains("_")) {
            return null;
        }

        return LocalTime.parse(value.trim());
    }

    private void loadDepartments() {
        Result<List<Department>> result = departmentBusiness.getAllDepartments();
        departments = result.isSuccess() ? result.getData() : new ArrayList<Department>();
        if (!globalPlanningAccess) {
            departments = departments.stream()
                    .filter(department -> managedDepartmentIds.contains(department.getId()))
                    .collect(Collectors.toList());
        }

        if (!result.isSuccess()) {
            log.warn("Unable to load departments for planning page");
        }
    }

    private void loadEmployees() {
        Result<List<EmployeeListDto>> result = employeeBusiness.getEmployeeList(false);
        employees = result.isSuccess() ? result.getData() : new ArrayList<EmployeeListDto>();
        if (!globalPlanningAccess) {
            List<String> managedDepartmentNames = departments.stream()
                    .map(Department::getDepartmentName).collect(Collectors.toList());
            employees = employees.stream()
                    .filter(employee -> managedDepartmentNames.contains(employee.getDepartmentName()))
                    .collect(Collectors.toList());
        }

        if (!result.isSuccess()) {
            log.warn("Unable to load employees for planning page");
        }
    }

    private void loadSelectedAssignments(Integer planningId) {
        Result<List<Employee>> result = planningBusiness.getAssignedEmployees(planningId);
        selectedEmployeeIds = result.isSuccess()
                ? result.getData().stream().map(Employee::getId).collect(Collectors.toList())
                : new ArrayList<Integer>();

        if (!result.isSuccess()) {
            log.warn("Unable to load employee assignments for planning id: " + planningId);
        }

        if (isCanManagePlanning() && planningId != null) {
            Result<List<PlanningsEmployee>> assignmentsResult = planningEmployeeBusiness.getActiveAssignments(planningId);
            planningAssignments = assignmentsResult.isSuccess() ? assignmentsResult.getData() : new ArrayList<PlanningsEmployee>();
        } else {
            planningAssignments = new ArrayList<>();
        }
    }

    public void saveAssignment(PlanningsEmployee assignment) {
        if (assignment == null) return;
        Result<Void> result = planningEmployeeBusiness.updateAssignment(
                assignment.getId(), assignment.getNote(), assignment.getPerformed());
        if (result.isSuccess()) {
            addInfoMessage("Affectation mise a jour.");
        } else {
            addErrorMessage("Impossible de sauvegarder l'affectation.");
        }
    }

    private void loadSelectedAssignment() {
        selectedAssignment = null;
        if (selectedPlanning == null || selectedPlanning.getId() == null || isCanManagePlanning()) {
            return;
        }
        Result<PlanningsEmployee> result = planningEmployeeBusiness.getAssignment(
                selectedPlanning.getId(), getConnectedEmployeeId());
        if (result.isSuccess()) {
            selectedAssignment = result.getData();
        }
    }

    private void loadSwapRequests() {
        Result<List<PlanningEmployeeSwapRequest>> result = planningEmployeeBusiness.getSwapRequests(
                getConnectedEmployeeId(), isCanManagePlanning());
        swapRequests = result.isSuccess() ? result.getData() : new ArrayList<PlanningEmployeeSwapRequest>();
        if (isDepartmentHeadManager()) {
            swapRequests = swapRequests.stream().filter(request -> request.getPlanningEmployee() != null
                    && request.getPlanningEmployee().getPlanning() != null
                    && request.getPlanningEmployee().getPlanning().getDepartment() != null
                    && managedDepartmentIds.contains(request.getPlanningEmployee().getPlanning().getDepartment().getId()))
                    .collect(Collectors.toList());
        }
        Result<List<PlanningSwapProposal>> proposalResult = planningEmployeeBusiness.getProposalsForEmployee(
                getConnectedEmployeeId());
        swapProposals = proposalResult.isSuccess() ? proposalResult.getData() : new ArrayList<PlanningSwapProposal>();
        if (!result.isSuccess()) {
            log.warn("Unable to load planning swap requests");
        }
    }

    private String swapErrorMessage(Result<?> result, String fallback) {
        if (result.getErrors() == null) return fallback;
        String key = result.getErrors().get("message");
        if ("planning.swap.error.pending".equals(key)) return "Une demande est deja en attente pour cette prestation.";
        if ("planning.swap.error.past".equals(key)) return "Une prestation passee ne peut plus faire l'objet d'un swap.";
        if ("planning.swap.error.type".equals(key)) return "Le swap est disponible uniquement pour une prestation de service.";
        if ("planning.swap.error.reason.required".equals(key)) return "Indiquez la raison de votre demande.";
        if ("planning.swap.error.reason.length".equals(key)) return "La raison est limitee a 500 caracteres.";
        if ("planning.swap.error.replacement".equals(key)) return "Selectionnez un autre employe comme remplacant.";
        if ("planning.swap.error.alreadyAssigned".equals(key)) return "Cet employe est deja affecte a cette prestation.";
        if ("planning.swap.error.conflict".equals(key)) return "Le remplacant possede deja un planning sur cette plage horaire.";
        if ("planning.swap.error.absence".equals(key)) return "Le remplacant est absent sur cette plage horaire.";
        if ("planning.swap.error.notPending".equals(key)) return "Cette demande a deja ete traitee.";
        if ("planning.swap.error.emergency.required".equals(key)) return "A moins de 24 h, activez le mode urgence.";
        if ("planning.swap.proposal.error.deadline".equals(key)) return "Le delai d'acceptation est depasse.";
        if ("planning.swap.proposal.error.unavailable".equals(key)) return "Cette proposition n'est plus disponible.";
        return fallback;
    }

    private Department findSelectedDepartment() {
        if (selectedDepartmentId == null || departments == null) {
            return null;
        }

        for (Department department : departments) {
            if (selectedDepartmentId.equals(department.getId())) {
                return department;
            }
        }

        return null;
    }

    private String resolveColor(String type) {
        if (type == null) {
            return "#3b82f6";
        }

        switch (type.trim().toUpperCase()) {
            case "REPOS":
                return "#22c55e";
            case "RECUP":
            case "RECUPERATION":
                return "#facc15";
            case "FERIE":
            case "HOLIDAY":
                return "#ef4444";
            case "VACANCES":
            case "CONGE":
                return "#f97316";
            default:
                return "#3b82f6";
        }
    }

    private String resolveTextColor(String type) {
        if (type != null) {
            String normalizedType = type.trim().toUpperCase();
            if ("RECUP".equals(normalizedType) || "RECUPERATION".equals(normalizedType)) {
                return "#422006";
            }
        }
        return "#ffffff";
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().validationFailed();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    public ScheduleModel getEventModel() {
        return eventModel;
    }

    public Planning getSelectedPlanning() {
        return selectedPlanning;
    }

    public void setSelectedPlanning(Planning selectedPlanning) {
        this.selectedPlanning = selectedPlanning;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public List<EmployeeListDto> getEmployees() {
        return employees;
    }

    public Integer getSelectedDepartmentId() {
        return selectedDepartmentId;
    }

    public void setSelectedDepartmentId(Integer selectedDepartmentId) {
        this.selectedDepartmentId = selectedDepartmentId;
    }

    public Integer getPlanningDepartmentFilterId() {
        return planningDepartmentFilterId;
    }

    public void setPlanningDepartmentFilterId(Integer planningDepartmentFilterId) {
        this.planningDepartmentFilterId = planningDepartmentFilterId;
    }

    public List<Integer> getSelectedEmployeeIds() {
        return selectedEmployeeIds;
    }

    public void setSelectedEmployeeIds(List<Integer> selectedEmployeeIds) {
        this.selectedEmployeeIds = selectedEmployeeIds;
    }

    public String getStartHourValue() {
        return startHourValue;
    }

    public void setStartHourValue(String startHourValue) {
        this.startHourValue = startHourValue;
    }

    public String getEndHourValue() {
        return endHourValue;
    }

    public void setEndHourValue(String endHourValue) {
        this.endHourValue = endHourValue;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public boolean isTypeSupportsAllDay() {
        return selectedPlanning != null && supportsAllDay(selectedPlanning.getType());
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getLocale() {
        return locale;
    }

    public String getMinTime() {
        return minTime;
    }

    public String getMaxTime() {
        return maxTime;
    }

    public String getSlotDuration() {
        return slotDuration;
    }

    public boolean isCanManagePlanning() {
        return globalPlanningAccess || !managedDepartmentIds.isEmpty();
    }

    public boolean isDepartmentHeadManager() {
        return !globalPlanningAccess && !managedDepartmentIds.isEmpty();
    }

    public boolean isCanChoosePlanningDepartment() {
        return globalPlanningAccess || managedDepartmentIds.size() > 1;
    }

    public boolean isGlobalPlanningAccess() {
        return globalPlanningAccess;
    }

    public List<PlanningsEmployee> getPlanningAssignments() { return planningAssignments; }

    public int getReportYear() { return reportYear; }
    public void setReportYear(int reportYear) { this.reportYear = reportYear; }
    public int getReportMonth() { return reportMonth; }
    public void setReportMonth(int reportMonth) { this.reportMonth = reportMonth; }
    public Integer getReportEmployeeId() { return reportEmployeeId; }
    public void setReportEmployeeId(Integer reportEmployeeId) { this.reportEmployeeId = reportEmployeeId; }
    public Integer getReportDepartmentId() { return reportDepartmentId; }
    public void setReportDepartmentId(Integer reportDepartmentId) { this.reportDepartmentId = reportDepartmentId; }

    public boolean isCanRequestSwap() {
        return selectedPlanning != null && selectedAssignment != null
                && "SERVICE".equalsIgnoreCase(selectedPlanning.getType())
                && selectedPlanning.getDate() != null && !selectedPlanning.getDate().isBefore(LocalDate.now())
                && getPendingSwapForSelectedPlanning() == null;
    }

    public PlanningEmployeeSwapRequest getPendingSwapForSelectedPlanning() {
        if (selectedPlanning == null || swapRequests == null) return null;
        for (PlanningEmployeeSwapRequest request : swapRequests) {
            if (request.getStatus() == PlanningSwapStatus.PENDING
                    && request.getPlanningEmployee() != null
                    && request.getPlanningEmployee().getPlanning() != null
                    && selectedPlanning.getId().equals(request.getPlanningEmployee().getPlanning().getId())) {
                return request;
            }
        }
        return null;
    }

    public String getSelectedHoursLabel() {
        if (selectedAssignment == null) return "-";
        return planningEmployeeBusiness.calculateHours(selectedAssignment).toPlainString() + " h";
    }

    public List<PlanningEmployeeSwapRequest> getSwapRequests() { return swapRequests; }
    public List<PlanningSwapProposal> getSwapProposals() { return swapProposals; }
    public List<PlanningSwapProposal> getSelectedSwapProposals() { return selectedSwapProposals; }
    public PlanningEmployeeSwapRequest getSelectedSwapRequest() { return selectedSwapRequest; }
    public void setSelectedSwapRequest(PlanningEmployeeSwapRequest value) { selectedSwapRequest = value; }
    public String getSwapReason() { return swapReason; }
    public void setSwapReason(String value) { swapReason = value; }
    public String getSwapReviewComment() { return swapReviewComment; }
    public void setSwapReviewComment(String value) { swapReviewComment = value; }
    public String[] getSwapReplacementEmployeeIds() { return swapReplacementEmployeeIds; }
    public void setSwapReplacementEmployeeIds(String[] value) {
        swapReplacementEmployeeIds = value == null ? new String[0] : value;
    }
    public boolean isSwapEmergencyMode() { return swapEmergencyMode; }
    public void setSwapEmergencyMode(boolean value) { swapEmergencyMode = value; }

    public String getSwapStatusCss(PlanningSwapStatus status) {
        return status == null ? "pending" : status.name().toLowerCase();
    }

    public String getSwapProposalStatusCss(PlanningSwapProposalStatus status) {
        return status == null ? "pending" : status.name().toLowerCase();
    }

    public String planningStatusLabel(Planning planning) {
        return planning == null || planning.getStatus() == null
                ? "Brouillon" : planning.getStatus().getLabel();
    }

    public String planningStatusCss(Planning planning) {
        return planning == null || planning.getStatus() == null
                ? "draft" : planning.getStatus().name().toLowerCase();
    }

    public boolean isSelectedPlanningDraft() {
        return selectedPlanning != null && (selectedPlanning.getStatus() == null
                || selectedPlanning.getStatus() == PlanningStatus.DRAFT);
    }

    public String getRecurrenceMode() { return recurrenceMode; }
    public void setRecurrenceMode(String recurrenceMode) { this.recurrenceMode = recurrenceMode; }
    public LocalDate getRecurrenceEndDate() { return recurrenceEndDate; }
    public void setRecurrenceEndDate(LocalDate recurrenceEndDate) { this.recurrenceEndDate = recurrenceEndDate; }
    public boolean isNewPlanning() { return selectedPlanning != null && selectedPlanning.getId() == null; }
    public boolean getNewPlanning() { return isNewPlanning(); }
    public LocalDate getCalendarInitialDate() { return calendarInitialDate; }

    public String formatSwapDate(PlanningEmployeeSwapRequest request) {
        if (request == null || request.getPlanningEmployee() == null
                || request.getPlanningEmployee().getPlanning() == null
                || request.getPlanningEmployee().getPlanning().getDate() == null) {
            return "-";
        }
        return request.getPlanningEmployee().getPlanning().getDate()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
