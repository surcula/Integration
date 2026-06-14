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
import be.atc.erpprojetintegration_1.entities.PlanningsEmployee;
import be.atc.erpprojetintegration_1.entities.PublicHoliday;
import be.atc.erpprojetintegration_1.enums.PlanningSwapStatus;
import be.atc.erpprojetintegration_1.tools.Result;
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
    private List<PlanningEmployeeSwapRequest> swapRequests;
    private PlanningEmployeeSwapRequest selectedSwapRequest;
    private String swapReason;
    private String swapReviewComment;
    private Integer swapReplacementEmployeeId;
    private String recurrenceMode = "NONE";
    private LocalDate recurrenceEndDate;
    private LocalDate calendarInitialDate;

    private String view = "timeGridWeek";
    private String locale = "fr";
    private String minTime = "00:00:00";
    private String maxTime = "24:00:00";
    private String slotDuration = "00:30:00";

    @PostConstruct
    public void init() {
        calendarInitialDate = LocalDate.now();
        if (isCanManagePlanning()) {
            loadDepartments();
            loadEmployees();
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
                ? planningBusiness.getAllActive()
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
        if (isCanManagePlanning() || selectedPlanning == null) {
            addErrorMessage("Cette action est reservee a l'employe affecte.");
            return;
        }
        Result<PlanningEmployeeSwapRequest> result = planningEmployeeBusiness.requestSwap(
                selectedPlanning.getId(), getConnectedEmployeeId(), swapReason);
        if (!result.isSuccess()) {
            addErrorMessage(swapErrorMessage(result, "Impossible d'envoyer la demande de swap."));
            return;
        }
        swapReason = null;
        loadSwapRequests();
        loadSelectedAssignment();
        addInfoMessage("Demande de swap envoyee a la RH.");
    }

    public void prepareSwapRequest() {
        swapReason = null;
    }

    public void selectSwapRequest(PlanningEmployeeSwapRequest request) {
        selectedSwapRequest = request;
        swapReplacementEmployeeId = request != null && request.getReplacementEmployee() != null
                ? request.getReplacementEmployee().getId() : null;
        swapReviewComment = request != null ? request.getReviewComment() : null;
    }

    public void approveSwap() {
        if (!isCanManagePlanning() || selectedSwapRequest == null) {
            addErrorMessage("Vous n'etes pas autorise a traiter cette demande.");
            return;
        }
        Result<Void> result = planningEmployeeBusiness.approve(selectedSwapRequest.getId(),
                swapReplacementEmployeeId, getConnectedEmployeeId(), swapReviewComment);
        if (!result.isSuccess()) {
            addErrorMessage(swapErrorMessage(result, "Impossible d'approuver la demande de swap."));
            return;
        }
        addInfoMessage("Swap approuve et planning mis a jour.");
        loadSwapRequests();
        loadPlannings();
        selectedSwapRequest = null;
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

        Result<Planning> result = planningBusiness.save(copy, new ArrayList<>(selectedEmployeeIds));
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

        Result<Void> result = planningBusiness.deactivate(selectedPlanning.getId());
        if (!result.isSuccess()) {
            addErrorMessage("Impossible de supprimer cet element du planning.");
            return;
        }

        calendarInitialDate = selectedPlanning.getDate();
        addInfoMessage("Element supprime du planning.");
        loadPlannings();
        prepareNewPlanning(calendarInitialDate.atStartOfDay());
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
                    selectedPlanning, selectedEmployeeIds, recurrenceDates);
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

        Result<Planning> result = planningBusiness.save(selectedPlanning, selectedEmployeeIds);
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
        allDay = false;
        selectedDepartmentId = planningDepartmentFilterId;
        selectedEmployeeIds = new ArrayList<>();
        recurrenceMode = "NONE";
        recurrenceEndDate = null;
        selectedEvent = null;
        selectedAssignment = null;
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
                ? planningBusiness.getById(planningId)
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

        if (!result.isSuccess()) {
            log.warn("Unable to load departments for planning page");
        }
    }

    private void loadEmployees() {
        Result<List<EmployeeListDto>> result = employeeBusiness.getEmployeeList(false);
        employees = result.isSuccess() ? result.getData() : new ArrayList<EmployeeListDto>();

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
        return authBean != null && authBean.isHrOrAdmin();
    }

    public boolean isCanRequestSwap() {
        return !isCanManagePlanning() && selectedPlanning != null && selectedAssignment != null
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
    public PlanningEmployeeSwapRequest getSelectedSwapRequest() { return selectedSwapRequest; }
    public void setSelectedSwapRequest(PlanningEmployeeSwapRequest value) { selectedSwapRequest = value; }
    public String getSwapReason() { return swapReason; }
    public void setSwapReason(String value) { swapReason = value; }
    public String getSwapReviewComment() { return swapReviewComment; }
    public void setSwapReviewComment(String value) { swapReviewComment = value; }
    public Integer getSwapReplacementEmployeeId() { return swapReplacementEmployeeId; }
    public void setSwapReplacementEmployeeId(Integer value) { swapReplacementEmployeeId = value; }

    public String getSwapStatusCss(PlanningSwapStatus status) {
        return status == null ? "pending" : status.name().toLowerCase();
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
